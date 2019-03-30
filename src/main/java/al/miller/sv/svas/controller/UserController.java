package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.UserService;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/genUri", method = GET)
    public URI generateURI() {
        return userService.generateAuthUri();
    }

    @RequestMapping(path = "/code", method = POST)
    public void addCode(@RequestBody String code) {
        userService.setCodeAndAuthAPI(code);
    }

    @RequestMapping(path = "/artists", method = GET)
    public List<Artist> getArtists() {
        return userService.getArtists();
    }

    @RequestMapping(path = "/playlists", method = GET)
    public List<PlaylistSimplified> getPlaylists(String playlistId){
        return userService.getPlaylists(playlistId);
    }

    @RequestMapping(path = "/tracks", method = GET)
    public List<Track> getTracks() {
        return userService.getTracks();
    }

    @RequestMapping(path = "/recommendations", method = GET)
    public Recommendations getRecommendations() {
        return userService.getRecommedations();
    }
}
