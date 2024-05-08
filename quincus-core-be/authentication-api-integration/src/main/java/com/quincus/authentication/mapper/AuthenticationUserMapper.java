package com.quincus.authentication.mapper;

import com.quincus.authentication.model.AuthenticationUser;
import com.quincus.authentication.model.TokenValidation;
import com.quincus.authentication.model.UserLogin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AuthenticationUserMapper {
    @Mapping(source = "id", target = "user.id")
    @Mapping(source = "roles", target = "user.roles")
    @Mapping(target = "user.fullName", expression = "java(userLogin.getFirstName() + \" \" + userLogin.getLastName())")
    AuthenticationUser userLoginToAuthUser(UserLogin userLogin);

    AuthenticationUser tokenValidationToAuthUser(TokenValidation tokenValidation);
}
