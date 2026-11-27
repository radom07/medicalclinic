package com.radom07.medicalclinic.controller;

import com.radom07.medicalclinic.command.CreateUserCommand;
import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.model.dto.UserDto;
import com.radom07.medicalclinic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get paginated list of users",
            description = "Retrieves a paginated and sorted list of users registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a page of users"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    })
    @GetMapping
    public PagedModel<UserDto> getUsers(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        log.info("Received GET /users request");
        return new PagedModel<>(userService.getUsers(pageable));
    }

    @Operation(summary = "Get a user by id", description = "Returns a user with provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id")
                               @Parameter(
                                       name = "id",
                                       description = "User's id",
                                       example = "1")
                               Long id) {
        log.info("Received GET /users/{} request", id);
        return userService.getById(id);
    }

    @Operation(summary = "Create a new user", description = "Creates a user account in system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))}),
            @ApiResponse(responseCode = "409", description = "User with provided email already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody CreateUserCommand command) {
        log.info("Received POST /users request for command={}", command);
        return userService.addUser(command);
    }

    @Operation(summary = "Delete a user by id", description = "Deletes a user from system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessageDto.class))})
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable("id")
                               @Parameter(
                                       name = "id",
                                       description = "User's id",
                                       example = "1")
                               Long id) {
        log.info("Received DELETE /users/{} request", id);
        userService.deleteById(id);
    }
}