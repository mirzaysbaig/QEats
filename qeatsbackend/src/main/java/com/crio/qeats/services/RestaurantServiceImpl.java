
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// this is a service layer 
@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
    /**
   * Get all the restaurants that are open now within a specific service radius.
   * - For peak hours: 8AM - 10AM, 1PM-2PM, 7PM-9PM
   * - service radius is 3KMs.
   * - All other times, serving radius is 5KMs.
   * - If there are no restaurants, return empty list of restaurants.
   * @param getRestaurantsRequest valid lat/long
   * @param currentTime current time.
   * @return GetRestaurantsResponse object containing a list of open restaurants or an
   *     empty list if none fits the criteria.
   */
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
      Double latitude=getRestaurantsRequest.getLatitude();
      Double longitude=getRestaurantsRequest.getLongitude();
      List<Restaurant> restaurant; // to store the list of restaurant taking from repositoryservice with the logic of it 
      int h=currentTime.getHour();
      int m=currentTime.getMinute();
      if((h>=8 && h<=9) || (h==10 && m==0) || h==13 || (h==14 && m==0) || (h>=19 && h<=21) || (h==21 && m==0) ){
         restaurant=restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, longitude, currentTime, peakHoursServingRadiusInKms );
      }
      else{
         restaurant=restaurantRepositoryService.findAllRestaurantsCloseBy(latitude, longitude, currentTime, normalHoursServingRadiusInKms);
      }
      // then storing the list of Restaurant in restaurants then passing it as getresponse body
      GetRestaurantsResponse restaurantsResponse=new GetRestaurantsResponse(restaurant);
     return restaurantsResponse;
  }


}

