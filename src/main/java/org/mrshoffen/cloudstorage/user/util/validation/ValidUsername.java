package org.mrshoffen.cloudstorage.user.util.validation;

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
@Size(min = 5, max = 20, message = "Длина имени пользователя должна быть между  {min} и {max} символами")
@NotBlank(message = "Username can't be empty!")
@Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$", message = "Недопустимые символы в имени пользователя")
public @interface ValidUsername {
    String message() default "Incorrect username";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
