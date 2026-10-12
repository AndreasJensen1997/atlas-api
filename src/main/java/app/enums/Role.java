package app.enums;

import io.javalin.security.RouteRole;

// TODO: Maybe add RouteRole interface?
public enum Role implements RouteRole {
    USER,
    ADMIN
}
