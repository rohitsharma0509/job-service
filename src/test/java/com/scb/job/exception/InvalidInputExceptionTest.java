package com.scb.job.exception;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InvalidInputExceptionTest {

	@Mock
    private  InvalidInputException  invalidInputExceptionTest;

    @Test
    void invalidInputExceptionTest_One_Arg() {
    	invalidInputExceptionTest = new InvalidInputException("test");
        assertNotNull(invalidInputExceptionTest);
    }
}
