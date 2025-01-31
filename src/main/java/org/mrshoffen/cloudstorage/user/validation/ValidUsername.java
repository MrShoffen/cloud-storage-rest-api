package org.mrshoffen.cloudstorage.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Size(min = 5, max = 20, message = "Incorrect name length! Must be between  {min} and {max}")
@NotBlank(message = "Username can't be empty!")
@Pattern(regexp = "^[a-zA-Z]+[a-zA-Z_]*[a-zA-Z.]+$", message = "Incorrect symbols in username!")
public @interface ValidUsername {
    String message() default "Incorrect username";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
