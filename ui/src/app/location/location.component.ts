import {Component} from '@angular/core';
import * as L from 'leaflet';

declare function require(url: string);

@Component({
  selector: 'app-location-page',
  templateUrl: './location.component.html',
  styleUrls: ['./location.component.css']
})

export class LocationComponent {

  positron = L.tileLayer('http://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}.png', {
    attribution: '©OpenStreetMap, ©CartoDB'
  });

  positronLabels = L.tileLayer('http://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}.png', {
    attribution: '©OpenStreetMap, ©CartoDB'
  });

  json = require('src/assets/countries.geo.json');

  geojson = L.geoJSON(this.json, {
    onEachFeature: function (feature, layer) {
      layer.bindPopup('<a class="btn btn-lg border-primary btn-secondary" style="color = #FFFF !important;" href="/location/' + feature.properties.name +
        ' "">Click to search ' + feature.properties.name + '<a>');
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
