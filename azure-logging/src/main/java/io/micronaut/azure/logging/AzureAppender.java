/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.azure.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.net.QueueFactory;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.util.Duration;
import io.micronaut.core.annotation.Internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Azure appender for Logback.
 *
 * @since 5.6
 */
@Internal
public class AzureAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    private static final int DEFAULT_QUEUE_SIZE = 128;
    private static final int DEFAULT_EVENT_DELAY_TIMEOUT = 100;
    private static final int DEFAULT_MAX_BATCH_SIZE = 128;
    private static final long DEFAULT_PUBLISH_PERIOD = 100;

    private final QueueFactory queueFactory = new QueueFactory();
    private final Duration eventDelayLimit = new Duration(DEFAULT_EVENT_DELAY_TIMEOUT);
    private final List<String> blackListLoggerNames = new ArrayList<>();
    private Encoder<ILoggingEvent> encoder;
    private Future<?> task;
    private BlockingDeque<ILoggingEvent> deque;
    private String source;
    private String subject;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private long publishPeriod = DEFAULT_PUBLISH_PERIOD;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private Appender<ILoggingEvent> emergencyAppender;
    private boolean configuredSuccessfully = false;

    /**
     * @param name the name
     */
    public void addBlackListLoggerName(String name) {
        blackListLoggerNames.add(name);
    }

    /**
     * @return the size
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * @param queueSize the size
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * @return publish period
     */
    public long getPublishPeriod() {
        return publishPeriod;
    }

    /**
     * @param publishPeriod publish period
     */
    public void setPublishPeriod(long publishPeriod) {
        this.publishPeriod = publishPeriod;
    }

    /**
     * @return max batch size
     */
    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     * @param maxBatchSize max batch size
     */
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the encoder
     */
    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    /**
     * @param encoder the encoder
     */
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }

        if (queueSize == 0) {
            addWarn("Queue size of zero is deprecated, use a size of one to indicate synchronous processing");
        }

        if (queueSize < 0) {
            addError("Queue size must be greater than zero");
            return;
        }

        if (publishPeriod <= 0) {
            addError("Publish period must be greater than zero");
            return;
        }

        if (encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }

        if (maxBatchSize <= 0) {
            addError("Max Batch size must be greater than zero");
            return;
        }

        if (emergencyAppender != null && !emergencyAppender.isStarted()) {
            emergencyAppender.start();
        }

        deque = queueFactory.newLinkedBlockingDeque(queueSize);

        task = getContext().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                dispatchEvents();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, publishPeriod, MILLISECONDS);

        super.start();
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        task.cancel(true);
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event == null || !isStarted() || blackListLoggerNames.contains(event.getLoggerName())) {
            return;
        }

        try {
            boolean inserted = deque.offer(event, eventDelayLimit.getMilliseconds(), MILLISECONDS);
            if (!inserted) {
                addInfo("Dropping event due to timeout limit of [" + eventDelayLimit + "] being exceeded");
                if (emergencyAppender != null) {
                    emergencyAppender.doAppend(event);
                }
            }
        } catch (InterruptedException e) {
            addError("Interrupted while appending event to SocketAppender", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> appender) {
        if (emergencyAppender == null) {
            emergencyAppender = appender;
        } else {
            addWarn("One and only one appender may be attached to " + getClass().getSimpleName());
            addWarn("Ignoring additional appender named [" + appender.getName() + "]");
        }
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        throw new UnsupportedOperationException("Don't know how to create iterator");
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        if (emergencyAppender != null && Objects.equals(name, emergencyAppender.getName())) {
            return emergencyAppender;
        }

        return null;
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return emergencyAppender == appender;
    }

    @Override
    public void detachAndStopAllAppenders() {
        if (emergencyAppender != null) {
            emergencyAppender.stop();
            emergencyAppender = null;
        }
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        if (emergencyAppender == appender) {
            emergencyAppender = null;
            return true;
        }

        return false;
    }

    @Override
    public boolean detachAppender(String name) {
        if (emergencyAppender != null && emergencyAppender.getName().equals(name)) {
            emergencyAppender = null;
            return true;
        }

        return false;
    }

    private void dispatchEvents() throws InterruptedException {
        if (!configuredSuccessfully && !tryToConfigure()) {
            return;
        }

        List<Object> entries = new ArrayList<>(maxBatchSize);
        List<ILoggingEvent> events = new ArrayList<>(maxBatchSize);

        while (!deque.isEmpty() && entries.size() < maxBatchSize) {
            var event = deque.takeFirst();
            events.add(event);
            entries.add(new LogEntry(new String(encoder.encode(event), UTF_8),
                event.getTimeStamp(), source, subject));
        }

        if (!entries.isEmpty() && !sendLogs(entries) && emergencyAppender != null) {
            for (ILoggingEvent event : events) {
                emergencyAppender.doAppend(event);
            }
        }
    }

    private boolean sendLogs(Iterable<Object> entries) {
        try {
            if (AzureLoggingClient.sendLogs(entries)) {
                return true;
            }
            addError("Sending log request failed");
        } catch (Exception e) {
            addError("Sending log request failed", e);
        }
        return false;
    }

    private boolean tryToConfigure() {

        if (!AzureLoggingClient.isReady()) {
            return false;
        }

        if (source == null) {
            source = AzureLoggingClient.getHost();
        }

        if (subject == null) {
            subject = AzureLoggingClient.getAppName();
        }

        configuredSuccessfully = true;

        return true;
    }
}
