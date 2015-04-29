/*
 * Copyright 2009-2010 LinkedIn, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.linkedin.norbert.network.partitioned

import com.linkedin.norbert.network.UnitConversions
import com.linkedin.norbert.network.common.RetryStrategy
import com.linkedin.norbert.network.javaobjects.{PartitionedRetrySpecification => JPartitionedRetrySpecification, RetrySpecification => JRetrySpecification}


/**
 * This is the companion object for the RoutingConfigs class.
 */
object RoutingConfigs {
  val defaultRoutingConfigs = new RoutingConfigs(false, false)
  def getDefaultRoutingConfigs():RoutingConfigs = {
    defaultRoutingConfigs
  }
}

/**
 * This class encapsulates the parameters used for request routing configurations.
 * @param SelectiveRetry This indicates whether or not we want to use a specific retry strategy.
 * @param DuplicatesOk This indicates whether or not we can have duplicates returned to a higher application layer.
 */
class RoutingConfigs(SelectiveRetry: Boolean, DuplicatesOk: Boolean ) {
  val selectiveRetry = SelectiveRetry
  val duplicatesOk = DuplicatesOk
}

/**
 * This is the companion object for the RetrySpecifications class.
 */
object RetrySpecification {
  def apply[ResponseMsg](maxRetry: Int = 0, callback: Option[Either[Throwable, ResponseMsg] => Unit] = None) = {
    new RetrySpecification[ResponseMsg](maxRetry, callback)
  }
}

/**
 * This class encapsulates the retry specifications for a request. This class is the non-partitioned version
 * which only contains two parameters. The class contains just a default constructor.
 *
 * @param maxRetry This is the maximum number of retry attempts for the request. If not otherwise specified, the value will be 0.
 * @param callback This is a method to be called with either a Throwable in the case of an error along
 *                 the way or a ResponseMsg representing the result.
 *
 * @throws IllegalArgumentException if the value for maxRetry is less than 0 and the callback is specified.
 */
class RetrySpecification[ResponseMsg](val maxRetry: Int,
                                                  val callback: Option[Either[Throwable, ResponseMsg] => Unit]) extends JRetrySpecification[ResponseMsg]{
  // Returns Int that is unboxed to int
  def getMaxRetry() = maxRetry
  // Returns an optional anonymous function
  def getCallback() = {
    val unitConversion = new UnitConversions[ResponseMsg]
    unitConversion.curryImplicitly(callback.getOrElse(Either => ()))
  }
}

/**
 * This is the companion object for the PartitionedRetrySpecification class.
 */
object PartitionedRetrySpecification {
  def apply[ResponseMsg](maxRetry: Int = 0,
                         callback: Option[Either[Throwable, ResponseMsg] => Unit] = None,
                         retryStrategy: Option[RetryStrategy] = None,
                         routingConfigs: RoutingConfigs = RoutingConfigs.defaultRoutingConfigs) = {
    new PartitionedRetrySpecification[ResponseMsg](maxRetry, callback, retryStrategy, routingConfigs)
  }
}

/**
 * This is the partitioned version of the RetrySpecification class which encapsulates retry specifications. This class contains
 * a default constructor and no additional functionality.
 *
 * @param maxRetry This is the maximum number of retry attempts for the request. If not otherwise specified, the value will be 0.
 * @param callback This is a method to be called with either a Throwable in the case of an error along
 *                 the way or a ResponseMsg representing the result.
 * @param retryStrategy This is the strategy to apply when we run into timeout situation.
 */
class PartitionedRetrySpecification[ResponseMsg](maxRetry: Int,
                                         callback: Option[Either[Throwable, ResponseMsg] => Unit],
                                         var retryStrategy: Option[RetryStrategy],
                                         var routingConfigs: RoutingConfigs = RoutingConfigs.defaultRoutingConfigs)
                                              extends JPartitionedRetrySpecification[ResponseMsg]{
  def getMaxRetry() = maxRetry
  def getCallback() = {
    val unitConversion = new UnitConversions[ResponseMsg]
    unitConversion.curryImplicitly(callback.getOrElse(Either => ()))
  }
  def getRetryStrategy() = retryStrategy
  def getRoutingConfigs() = routingConfigs
}