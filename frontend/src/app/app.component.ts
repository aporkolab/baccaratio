import { Component } from '@angular/core';

import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [RouterModule],
})
export class AppComponent {
  title = 'Baccaratio';
  showModal: boolean = false;

  showAuthorModal(): void {
    this.showModal = true;
  }

  hideModal(): void {
    this.showModal = false;
  }
}
