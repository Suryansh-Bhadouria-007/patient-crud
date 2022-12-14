/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2020 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by all applicable intellectual property
 * laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * author: suryansh
 * date: 08/10/22
 **************************************************************************/

package com.suryansh.patientcrud;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suryansh.patientcrud.controller.PatientRecordController;
import com.suryansh.patientcrud.entity.PatientRecord;
import com.suryansh.patientcrud.exception.InvalidRequestException;
import com.suryansh.patientcrud.exception.ResourceNotFoundException;
import com.suryansh.patientcrud.repository.PatientRecordRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(PatientRecordController.class)
/**
 * 1. @SpringBootTest annotation loads the full application context so that we are able to test various components.
 *    So basically, the @SpringBootTest annotation tells Spring Boot to look for the main configuration class
 *    (one with @SpringBootApplication, for instance) and use that to start a Spring application context.
 *
 *    @WebMvcTest annotation loads only the specified controller and its dependencies only without loading the entire application.
 *    For example, let's say you have multiple Spring MVC controllers in your Spring boot project
 *    - EmployeeController, UserController, LoginController, etc. then we can use @WebMvcTest annotation to test
 *    only specific Spring MVC controllers without loading all the controllers and their dependencies.
 *
 * 2. Spring Boot provides @SpringBootTest annotation for Integration testing.
 *
 *    Spring boot provides @WebMvcTest annotation for testing Spring MVC controllers (Unit testing).
 */
public class PatientRecordControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    PatientRecordRepository patientRecordRepository;

    PatientRecord RECORD_1 = new PatientRecord(1l, "Kopal Niranjan", 23, "Lucknow India");
    PatientRecord RECORD_2 = new PatientRecord(2l, "Suryansh Bhadouria", 27, "Lucknow India");
    PatientRecord RECORD_3 = new PatientRecord(3l, "Joan Arc", 31, "New York USA");

    @Test
    public void getAllRecords_success() throws Exception {
        List<PatientRecord> records = new ArrayList<>(Arrays.asList(RECORD_1, RECORD_2, RECORD_3));

        Mockito.when(patientRecordRepository.findAll()).thenReturn(records);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/patient")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[2].name", is("Joan Arc")));
    }

    @Test
    public void getPatientById_success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId())).thenReturn(java.util.Optional.of(RECORD_1));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.name", is("Kopal Niranjan")));
    }

    @Test
    public void createRecord_success() throws Exception {
        PatientRecord record = PatientRecord.builder()
            .name("John Arc")
            .age(47)
            .address("New York USA")
            .build();

        Mockito.when(patientRecordRepository.save(record)).thenReturn(record);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(record));

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.name", is("John Arc")));
    }

    @Test
    public void updatePatientRecord_success() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
            .patientId(1l)
            .name("Baby Kopal")
            .age(23)
            .address("Bangalore India")
            .build();

        Mockito.when(patientRecordRepository.findById(RECORD_1.getPatientId())).thenReturn(Optional.of(RECORD_1));
        Mockito.when(patientRecordRepository.save(updatedRecord)).thenReturn(updatedRecord);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.name", is("Baby Kopal")))
            .andExpect(jsonPath("$.address", is("Bangalore India")));
    }

    @Test
    public void updatePatientRecord_nullId() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
            .name("Sherlock Holmes")
            .age(40)
            .address("221B Baker Street")
            .build();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
            .andExpect(status().isBadRequest())
            .andExpect(result ->
                assertTrue(result.getResolvedException() instanceof InvalidRequestException))
            .andExpect(result ->
                assertEquals("PatientRecord or ID must not be null!", result.getResolvedException().getMessage()));
    }

    @Test
    public void updatePatientRecord_recordNotFound() throws Exception {
        PatientRecord updatedRecord = PatientRecord.builder()
            .patientId(5l)
            .name("Sherlock Holmes")
            .age(40)
            .address("221B Baker Street")
            .build();

        Mockito.when(patientRecordRepository.findById(updatedRecord.getPatientId())).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(updatedRecord));

        mockMvc.perform(mockRequest)
            .andExpect(status().isNotFound())
            .andExpect(result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
            .andExpect(result ->
                assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()));
    }

    @Test
    public void deletePatientById_success() throws Exception {
        Mockito.when(patientRecordRepository.findById(RECORD_2.getPatientId())).thenReturn(Optional.of(RECORD_2));

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/patient/2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void deletePatientById_notFound() throws Exception {
        Mockito.when(patientRecordRepository.findById(5l)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/patient/5")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result ->
                assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
            .andExpect(result ->
                assertEquals("Patient with ID 5 does not exist.", result.getResolvedException().getMessage()));
    }
}