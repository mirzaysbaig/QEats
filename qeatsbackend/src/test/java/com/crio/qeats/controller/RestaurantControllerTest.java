
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import static com.crio.qeats.controller.RestaurantController.CART_API;
import static com.crio.qeats.controller.RestaurantController.CART_CLEAR_API;
import static com.crio.qeats.controller.RestaurantController.CART_ITEM_API;
import static com.crio.qeats.controller.RestaurantController.GET_ORDERS_API;
import static com.crio.qeats.controller.RestaurantController.MENU_API;
import static com.crio.qeats.controller.RestaurantController.POST_ORDER_API;
import static com.crio.qeats.controller.RestaurantController.RESTAURANTS_API;
import static com.crio.qeats.controller.RestaurantController.RESTAURANT_API_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Pass all the RestaurantController test cases.
//  Make modifications to the tests if necessary.,
//  Test RestaurantController by mocking RestaurantService

// it means that for this test to run we need to start the spring boot server 
// it shows go to class mention and start the main application 
@SpringBootTest(classes = {QEatsApplication.class}) // it refers to which classes to go for testing 
//mockito setting is done to define the strictness of it 
// if the mock has not called then mockito will say that we have mock but it is not called then we need to fix it 
@MockitoSettings(strictness = Strictness.STRICT_STUBS) 
//it mimics mvc and creaate a mockk of it 
@AutoConfigureMockMvc 
// one spring test is executed then it will kill the server with @dities context
// as one server will start it doesnt kill the teest 
@DirtiesContext 
// if we say active profiles is test then it will pick up application-test-properties for its onfiguration
// we also have configuration of main class 
@ActiveProfiles("test") 
public class RestaurantControllerTest {

  //FIXME: REVIEW the api names
  private static final String RESTAURANT_API_URI = RESTAURANT_API_ENDPOINT + RESTAURANTS_API;
  private static final String MENU_API_URI = RESTAURANT_API_ENDPOINT + MENU_API;
  private static final String CART_API_URI = RESTAURANT_API_ENDPOINT + CART_API;
  private static final String ADD_REMOVE_CART_API_URI = RESTAURANT_API_ENDPOINT + CART_ITEM_API;
  private static final String CLEAR_CART_API_URI = RESTAURANT_API_ENDPOINT + CART_CLEAR_API;
  private static final String POST_ORDER_API_URI = RESTAURANT_API_ENDPOINT + POST_ORDER_API;
  private static final String LIST_ORDERS_API_URI = RESTAURANT_API_ENDPOINT + GET_ORDERS_API;

  private static final String FIXTURES = "fixtures/exchanges";
  private ObjectMapper objectMapper;

  // what is mockmvc?
  //MockMvc is a part of Spring Test framework that allows testing Spring MVC controllers
  // without running a full web server.
  //It mocks the HTTP request-response cycle so that controllers can be tested without deploying the application.

  private MockMvc mvc;

  // it is just like autowired to mock a spring bmanaged bean or use as a mock to manage the bean dependency in spring 
  @MockBean
  private RestaurantService restaurantService;

// it injects @mock dependencies into the real object or help in creating mock object 
// It helps in creating a testable controller instance.
/*@InjectMocks injects the mocked dependencies (@MockBean instances) 
into the actual RestaurantController instance. */
  @InjectMocks
  private RestaurantController restaurantController;

  // test would run before your test everytime 
  // initializing mock, objectmapper and mockmvc
  @BeforeEach
  public void setup() {
    objectMapper = new ObjectMapper();

    // Initializes mock annotations such as @Mock and @InjectMocks.
    // Note: `initMocks` is deprecated; use `MockitoAnnotations.openMocks(this);` instead.
    MockitoAnnotations.initMocks(this);
  
    //MockMvc is a powerful tool for testing Spring MVC controllers without deploying a full application.
     //Standalone setup (MockMvcBuilders.standaloneSetup(controller)) is used for unit testing controllers in isolation.
     //Full context setup (MockMvcBuilders.webAppContextSetup(webApplicationContext)) is used for integration testing.
    // Configuring MockMvc to test the controller in isolation without loading the entire Spring context.
    mvc = MockMvcBuilders.standaloneSetup(restaurantController).build();
    
  }

  // it uses Query param 
  @Test
  public void invalidLatitudeResultsInBadHttpRequest() throws Exception {
    // making of uri using query parameter using restaurant api 
    // Constructing a URI with invalid latitude (91) using UriComponentsBuilder.
    // sedning thebreguest using query parameter 
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "91")
        .queryParam("longitude", "20")
        .build().toUri();
 // checking the if uri generated is tru or not 
    assertEquals(RESTAURANT_API_URI + "?latitude=91&longitude=20", uri.toString());

    // simulate the spring server and api call calling mock mvc going through whole pipeline
     // Simulating an HTTP GET request using MockMvc and retrieving the response.
     // it test for the alone mockmvc for controller and not runing the ful applications 

    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();
     
    // check the response is correct or not 
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "-91")
        .queryParam("longitude", "20")
        .build().toUri();
   // validating our uri is correct 
    assertEquals(RESTAURANT_API_URI + "?latitude=-91&longitude=20", uri.toString());
// call to simulate the actual http request 
// it would simulate the whole pipeline and make sure it will send whole response 
    response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();
  // asserting the response code what we got
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  // now we can use verify to verify the response send with the given mock response 
  // we can also capture the response using argumen tcaptor 

  }

  //-90 TO 90 latitude


  // this test i ssame as above test to check invalid longitude and it also has the same function
  // Refer the comment above for understanding this 
  @Test
  public void invalidLongitudeResultsInBadHttpRequest() throws Exception {
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "10")
        .queryParam("longitude", "181")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitude=181", uri.toString());


    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "10")
        .queryParam("longitude", "-181")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitude=-181", uri.toString());

    // calling api with invalid longitude
    response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }



  // checking for mispelled word as it would not be validate with the correct word in http request 
  @Test
  public void incorrectlySpelledLongitudeParamResultsInBadHttpRequest() throws Exception {
    // mocks not required, since validation will fail before that.
   /*  Mocks are unnecessary because the error occurs before the controller calls the service layer (RestaurantService).

    Spring's validation rejects the request immediately if a required query parameter is missing or misspelled.
    Since the request never reaches the service layer, mocking RestaurantService is not needed.
    The test only checks request validation (query parameters) and ensures a 400 Bad Request response.
   */
 // we used mock for restaurant service only na 
 // @mockbean for restaurantservice  dependencies

    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "10")
        .queryParam("longitue", "20")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=10&longitue=20", uri.toString());


    // MockHttpServletResponse response = mvc.perform(
    //     get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    // ).andReturn().getResponse();

    // // Since longitue is not a recognized parameter, the API should reject the request and return an HTTP 400 (Bad Request).
    // assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }


  // same as previous mispelled testcasse 
  // this is for lattitude as laitude
  @Test
  public void incorrectlySpelledLatitudeParamResultsInBadHttpRequest() throws Exception {
    // mocks not required, since validation will fail before that.
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("laitude", "10")
        .queryParam("longitude", "20")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?laitude=10&longitude=20", uri.toString());


    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  // no parameter so request will fail
  /*In a properly configured Spring REST API, if a required request parameter is missing, 
  Spring's validation will fail before reaching the service layer (RestaurantService). */
  @Test
  public void noRequestParamResultsInBadHttpReuest() throws Exception {
    // mocks not required, since validation will fail before that.
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .build().toUri();

    assertEquals(RESTAURANT_API_URI, uri.toString());

    // calling api without latitude and longitude
    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }


  // same tupe only one parameter passd still request fails 
  @Test
  public void missingLongitudeParamResultsInBadHttpRequest() throws Exception {
    // calling api without latitude
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("latitude", "20.21")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?latitude=20.21", uri.toString());

    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }


  // same as previous ytetscase 
  @Test
  public void missingLatitudeParamResultsInBadHttpRequest() throws Exception {
    // calling api without longitude
    URI uri = UriComponentsBuilder
        .fromPath(RESTAURANT_API_URI)
        .queryParam("longitude", "30.31")
        .build().toUri();

    assertEquals(RESTAURANT_API_URI + "?longitude=30.31", uri.toString());

    MockHttpServletResponse response = mvc.perform(
        get(uri.toString()).accept(APPLICATION_JSON_UTF8)
    ).andReturn().getResponse();

    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }




}

