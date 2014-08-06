package org.mifosplatform.portfolio.activationprocess.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class ActivationProcessCommandFromApiJsonDeserializer {

	/**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("fullname", "phone", "city", "device", "email"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ActivationProcessCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("activationprocess");

        final JsonElement element = fromApiJsonHelper.parse(json);
        
        final String fullname = fromApiJsonHelper.extractStringNamed("fullname", element);
        baseDataValidator.reset().parameter("fullname").value(fullname).notBlank().notExceedingLengthOf(50);
        
        final Long phone = fromApiJsonHelper.extractLongNamed("phone", element);
        baseDataValidator.reset().parameter("phone").value(phone).notNull().integerGreaterThanZero();

        final String city = fromApiJsonHelper.extractStringNamed("city", element);
        baseDataValidator.reset().parameter("city").value(city).notBlank().notExceedingLengthOf(50);
        
        final String device = fromApiJsonHelper.extractStringNamed("device", element);
        baseDataValidator.reset().parameter("device").value(device).notBlank().notExceedingLengthOf(18);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");
        
        final JsonElement element = fromApiJsonHelper.parse(json);
        if (fromApiJsonHelper.parameterExists("officeId", element)) {
            final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);
            baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();
        }
        
        if (fromApiJsonHelper.parameterExists("firstname", element)) {
            final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
            baseDataValidator.reset().parameter("firstname").value(firstname).notBlank().notExceedingLengthOf(50);
        }

        if (fromApiJsonHelper.parameterExists("lastname", element)) {
            final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
            baseDataValidator.reset().parameter("lastname").value(lastname).notBlank().notExceedingLengthOf(50);
        }

        if (fromApiJsonHelper.parameterExists("isLoanOfficer", element)) {
            final Boolean loanOfficerFlag = fromApiJsonHelper.extractBooleanNamed("isLoanOfficer", element);
            baseDataValidator.reset().parameter("isLoanOfficer").value(loanOfficerFlag).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
