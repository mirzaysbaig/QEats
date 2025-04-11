/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;


@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {


  
  // Tells Spring to automatically give us a MongoTemplate object.
// MongoTemplate is a helper that lets us talk to the MongoDB database
// (like saving, updating, finding, or deleting data)  CRUD Operations.
  @Autowired
  private MongoTemplate mongoTemplate;

  // it Tells Spring to automatically give us a Provider for ModelMapper.
// ModelMapper is used to copy data from one object to another (like DTO to Entity).
// Provider allows us to get a fresh ModelMapper instance when we need it by calling modelMapperProvider.get()
  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  // for fetching the restaurant repo service which has restaurant entity type 
  @Autowired
  private RestaurantRepository restaurantRepository;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.

  
  
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<>(); // it is for DTO object which is used for serialization and map to it 
    // it contains the field only which need to be shown to client
    // it would be mapped from database to dto using model mapper 
    // first we will querry from using mongotemplate 
    
    // now to fetch from db which will return restaurant entity type 
    List<RestaurantEntity> restaurantEntities=new ArrayList<>();
     

    try {
      restaurantEntities = restaurantRepository.findAll();
      
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    ModelMapper mapperClass = modelMapperProvider.get(); // to map the details with the restaurant class dto
    for (RestaurantEntity tmp : restaurantEntities) {
      restaurants.add(mapperClass.map(tmp, Restaurant.class)); // now mapping done ;
    
    }

    // now mylist to keep the filter data based on the given lat long 
    List<Restaurant> myList=new ArrayList<>();
    
    for (Restaurant res : restaurants) { // loop in restaurants got from database
      double restaurantLat = res.getLatitude();
      double restaurantLon = res.getLongitude();
      LocalTime openAt = LocalTime.parse(res.getOpensAt());
      LocalTime closeAt = LocalTime.parse(res.getClosesAt());
      double distanceKm = GeoUtils.findDistanceInKm(
                            latitude, longitude, restaurantLat, restaurantLon);
      if (Double.compare(distanceKm, servingRadiusInKms) > 0) {
        continue;
      }
      boolean result = isValidTime(currentTime, openAt, closeAt);  // just checking if is valid time o not 
      if (result == false) {
        continue;
      }
      myList.add(res); // adding to my list 

    }
    return myList;


    

      //CHECKSTYLE:OFF
      //CHECKSTYLE:On
  }

  private boolean isValidTime(LocalTime current, 
  LocalTime open, LocalTime close) {
  boolean result = false;
  if (current.equals(open) || current.equals(close)) {
  result = true;
  }
  if (current.isAfter(open) && current.isBefore(close)) {
  result = true;
  }
  return result;
  }







  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}

