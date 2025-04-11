
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Pass all the RestaurantService test cases.
// Contains necessary test cases that check for implementation correctness.
// Objectives:
// 1. Make modifications to the tests if necessary so that all test cases pass
// 2. Test RestaurantService Api by mocking RestaurantRepositoryService.

//
@SpringBootTest(classes = {QEatsApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
// one spring test is executed then it will kill the server with @dities context
// as one server will start it doesnt kill the teest 
@DirtiesContext
// saying to use application properties of test file
@ActiveProfiles("test") 
class RestaurantServiceTest {

  private static final String FIXTURES = "fixtures/exchanges";
  @InjectMocks // it means @mockbean should be injected here or managing mockbean dependencies here as @mockbean field has dependencies
  // in restaurnt serviceimpl
  private RestaurantServiceImpl restaurantService;
  @MockBean // same as autowired 
  private RestaurantRepositoryService restaurantRepositoryServiceMock;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);

    objectMapper = new ObjectMapper();
  }


  private String getServingRadius(List<Restaurant> restaurants, LocalTime timeOfService) {
    // it uses Mockito to simulate calss to restaurant repository repository
    // taking 4 imput lattitude, longitude.. 
    // it then return the prestore response as it is the mock object 
    // is this just mocking enough no , how to know correct data is comming 
    // so we use Mockito.verify for verification 
    // argumentcaptor for capturing the response 
    // finding  find all restuarants closes by using mock 
    //ist test check 

    /*Instead of querying a real database, it returns a predefined restaurant list when called.
    Ensures that the test runs without needing actual database data.  
    */
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

 //2nd test check
 /*Calls the real service method (restaurantService.findAllRestaurantsCloseBy(...)).
The service then calls the mocked repository method. */
    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //LocalTime.of(19,00));

    assertEquals(2, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());

    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    // apart from assertion we can do  
    // verifying that one time when called restaurantRepositoryServiceMock ado it gives data or just giving dummy data 
    // now going more in depth 
    // now capturing the value what is sendin serving radius atleast once which capture the response 
    // now then we can test it further 
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    return servingRadiusInKms.getValue().toString();
  }

  @Test
  void peakHourServingRadiusOf3KmsAt7Pm() throws IOException {
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(19, 0)), "3.0");
  }


  @Test
  void normalHourServingRadiusIs5Kms1() throws IOException {

    // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
    // We must ensure the API retrieves only restaurants that are closeby and are open
    // In short, we need to test:
    // 1. If the mocked service methods are being called
    // 2. If the expected restaurants are being returned
    // HINT: Use the `loadRestaurantsDuringNormalHours` utility method to speed things up
    
    List<Restaurant> restaurants=loadRestaurantsDuringNormalHours();
    // for mockresponse if the restauran is not there then showing mock response 
    when(restaurantRepositoryServiceMock
    .findAllRestaurantsCloseBy
    (any(Double.class),any(Double.class),any(LocalTime.class),any(Double.class)))
    .thenReturn(restaurants);

    GetRestaurantsResponse restaurantsResponse=restaurantService
                                                .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0,30.0),LocalTime.of(22,0));
    
    assertEquals(3, restaurantsResponse.getRestaurants().size());
    assertEquals("10", restaurantsResponse.getRestaurants().get(0).getRestaurantId());
    assertEquals("11", restaurantsResponse.getRestaurants().get(1).getRestaurantId());
    assertEquals("12", restaurantsResponse.getRestaurants().get(2).getRestaurantId());
                                                                                       
    // for checking serving radius 
    ArgumentCaptor<Double> Servingradius=ArgumentCaptor.forClass(Double.class);
    // to capture the serving radius and verify that one time it hits 
    verify(restaurantRepositoryServiceMock,times(1))
    .findAllRestaurantsCloseBy(any(Double.class),any(Double.class),
     any(LocalTime.class),Servingradius.capture());
     assertEquals(Servingradius.getValue().toString(),"5.0");
  }
  @Test
  void normalHourServingRadiusIs5Kms() throws IOException {
    List<Restaurant> restaurants = loadRestaurantsDuringNormalHours();
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);
    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0), LocalTime.of(22, 0));
   // assertEquals(3, allRestaurantsCloseBy.getRestaurants().size());
    
    assertEquals("10", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(2).getRestaurantId());
    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());
    assertEquals(servingRadiusInKms.getValue().toString(), "5.0");
  }



  
  private List<Restaurant> loadRestaurantsDuringNormalHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/normal_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsSearchedByAttributes() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/list_restaurants_searchedby_attributes.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsDuringPeakHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/peak_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }
}
