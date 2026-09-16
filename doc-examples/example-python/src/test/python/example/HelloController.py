from micronaut.http import MediaType
from micronaut.http.annotation import Controller, Get, Produces


@Controller("/hello")
class HelloController:

    @Get
    @Produces(MediaType.TEXT_PLAIN)
    def index(self) -> str:
        return "Hello World"
