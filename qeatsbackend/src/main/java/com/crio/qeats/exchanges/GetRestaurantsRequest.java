/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Implement GetRestaurantsRequest.
//  Complete the class such that it is able to deserialize the incoming query params from
//  REST API clients.
//  For instance, if a REST client calls API
//  /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
//  this class should be able to deserialize lat/long and optional searchFor from that.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRestaurantsRequest {

@NotNull(message="lattitude can not be Null or empty")
@Min(value= -90,message="Minimum lattitude is -90")
@Max(value= 90, message="maximum latitude is 90")
private Double latitude;

@NotNull(message="longitude cannot be null or empty")
@Min(value=-180,message="Minimum Longitude is -180")
@Max(value=180, message="maximum Longitude is 180")
private Double longitude;

//private String searchFor;
}

