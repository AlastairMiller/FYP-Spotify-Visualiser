import {AfterViewInit, Component} from '@angular/core';
import {geoJSON, latLng, tileLayer} from 'leaflet';
import * as L from 'leaflet';

declare function require(url: string);

@Component({
  selector: 'app-explore-page',
  templateUrl: './explore.component.html',
  styleUrls: ['./explore.component.css']
})

export class ExploreComponent {

  currentCountry;

  positron = L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
    attribution: '©OpenStreetMap, ©CartoDB'
  });

  positronLabels = L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
    attribution: '©OpenStreetMap, ©CartoDB'
  });

  json = require('src/assets/countries.geo.json');

  geojson = L.geoJSON(this.json, {
    onEachFeature: function (feature, layer) {
      layer.bindPopup('<h1>' + feature.properties.name + '</h1>' + '<button type="button" class="btn btn-primary">Primary</button>');
    }

  });

  options = {
    layers: [
      this.positron,
      this.positronLabels,
      this.geojson
    ],
    zoom: 3,
    minZoom: 2,
    center: L.latLng([30, 0])
  };

}
