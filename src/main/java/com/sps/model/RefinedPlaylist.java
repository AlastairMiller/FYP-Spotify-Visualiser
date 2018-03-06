package com.sps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class RefinedPlaylist implements Serializable {
    @Id
    private String id;
    private URI uri;
    private URL externalURL;
    private Integer numOfFollowers;
    private String href;
    @ElementCollection
    private List<String> images;
    private String name;
    @ManyToOne(targetEntity = RefinedUser.class)
    private RefinedUser refinedUser;
    @ManyToMany(targetEntity = RefinedTrack.class)
    private List<RefinedTrack> refinedTracks;
}
