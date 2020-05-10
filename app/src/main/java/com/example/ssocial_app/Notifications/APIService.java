package com.example.ssocial_app.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA_ZaWgsQ:APA91bGC7lwP2b2SKnfXkWdmzDtEjIrK1jT52eqCh5mebcuCA9YQYQch6gDHF8S2RSt8EE7Gk7WnLqY7fK9WCwHF43WCwulINZ3eRVIAeIOHhE0hqkJ4W2UFqc5qQrt5Chv2qG7Ip6xN"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body) ;


}
