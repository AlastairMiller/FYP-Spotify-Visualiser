package com.sps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class RefinedArtist implements Serializable {
    @Id
    private String id;
    private URL externalURL;
    @ElementCollection
    private List<String> genres;
    @ManyToMany(targetEntity = RefinedArtist.class)
    private List<RefinedArtist> refinedArtists;
    private String href;
    private String name;
    private String type;
    private int followers;
    private int popularity;
    private URI uri;
}
