package com.techdisqus.dto.validators;

import com.techdisqus.dto.CreatePostRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CreatePostRequestValidator
        implements ConstraintValidator<CreatePostRequestConstraint, CreatePostRequest> {
    @Override
    public boolean isValid(CreatePostRequest createPostRequest,
                           ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
