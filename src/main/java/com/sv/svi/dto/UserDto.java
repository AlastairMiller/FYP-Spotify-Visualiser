package com.sv.svi.dto;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.users_profile.GetUsersProfileRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDto {

    public static User pullUser(SpotifyApi api, String id) {
        //Todo MakeAsync
        final GetUsersProfileRequest request = api.getUsersProfile(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download user metadata with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

}
