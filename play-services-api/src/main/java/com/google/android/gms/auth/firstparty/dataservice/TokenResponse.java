/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.auth.firstparty.dataservice;

import org.microg.safeparcel.AutoSafeParcelable;

public class TokenResponse extends AutoSafeParcelable {
    @Field(1)
    public String access_token;
    @Field(2)
    public String token_type;
    @Field(3)
    public Integer expires_in;
    @Field(4)
    public String refresh_token;
    @Field(5)
    public String scope;
    @Field(6)
    public String id_token;
    @Field(7)
    public String error;
    @Field(8)
    public String error_description;

    public static final Creator<TokenResponse> CREATOR = new AutoCreator<TokenResponse>(TokenResponse.class);
}
