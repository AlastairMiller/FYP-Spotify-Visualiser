import {Component, ElementRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {Chart} from 'chart.js';
import * as palette from 'google-palette';


@Component({
  selector: 'app-basic-chart',
  templateUrl: './basic-chart.component.html',
  styleUrls : ['./basic-chart.component.css']
})
export class BasicChartComponent implements OnInit, OnChanges {
  @Input()
  public input: Map<String, number>;
  @Input()
  public title: string;
  public chart: Chart = null;

  @ViewChild('chart')
  public canvasRef: ElementRef;

  public context: CanvasRenderingContext2D;

  constructor() {
  }

  clearChart() {
    this.chart.clear();
    this.chart.destroy();
    const ctx: CanvasRenderingContext2D =
      this.canvasRef.nativeElement.getContext('2d');
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
  }

  ngOnInit() {

  }

  ngOnChanges() {
    this.init();
  }

  init() {
    this.context = CanvasRenderingContext2D =
      this.canvasRef.nativeElement.getContext('2d');

    const data = [];
    const labels = [];

    if (this.chart) {
      this.clearChart();
    }

    this.input.forEach(kp => {
      labels.push(kp['_id']);
      data.push(kp['value']);
    });

    this.chart = new Chart(this.context, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: data,
            backgroundColor: palette('rainbow', data.length).map(function (hex) {
              return '#' + hex;
            })
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        legend: {
          display: true,
          labels: {
            fontColor: '#fff',
            fontSize: 16
          },
          position: 'bottom'
        }
      }
    });

  }

}
