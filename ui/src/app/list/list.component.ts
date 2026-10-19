import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';


@Component({
  selector: 'app-list-page',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css'],
  animations: [
    trigger('visibilityChanged', [
      state('shown', style({opacity: 1})),
      state('hidden', style({opacity: 0})),
      transition('* => *', animate('1s'))
    ])
  ]
})
export class ListComponent implements OnChanges {
  @Input() isVisible = true;
  visibility = 'shown';

  ngOnChanges() {
    this.visibility = this.isVisible ? 'shown' : 'hidden';
  }
}
