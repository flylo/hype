/*-
 * -\-\-
 * hype-submitter
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.hype.model;

import static com.spotify.hype.model.ResourceRequest.CPU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RunEnvironmentTest {

  @Test
  public void overrideResourceRequestsForSame() throws Exception {
    RunEnvironment env = RunEnvironment.get()
        .withRequest(CPU.of("1"))
        .withRequest(CPU.of("7"));

    assertThat(env.resourceRequests().size(), equalTo(1));
    assertThat(env.resourceRequests(), hasEntry("cpu", "7"));
  }
}
