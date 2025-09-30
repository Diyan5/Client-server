package registrationApp.model;

public record User(long id, String name, String email, byte[] passHash, byte[] passSalt) {}
