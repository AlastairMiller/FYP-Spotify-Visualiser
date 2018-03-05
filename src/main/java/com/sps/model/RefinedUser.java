package com.sps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RefinedUser implements Serializable {
    @Id
    private String id;
    private URI uri;
    private String displayName;
    private URL externalUrl;
    private Integer numOfFollowers;
    private String href;
    @ElementCollection
    private List<String> images;
}
