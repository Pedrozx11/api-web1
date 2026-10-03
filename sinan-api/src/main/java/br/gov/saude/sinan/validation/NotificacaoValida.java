package br.gov.saude.sinan.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Regras de negócio que dependem de mais de um campo (RN02, RN03 e consistência de datas). */
@Documented
@Constraint(validatedBy = NotificacaoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotificacaoValida {

    String message() default "Notificação inválida.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
