import {Component, OnInit, ViewChild} from '@angular/core';
import {RestService} from '../../services/rest.service';
import {Router} from '@angular/router';
import {AlertComponent} from '../alert/alert.component';
import {ClarityIcons, trashIcon} from '@cds/core/icon';
import {Item} from "../../models/item";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: []
})
export class HomeComponent implements OnInit {

  items: Item[] = [];
  itemCount = 0;
  // @ts-ignore
  @ViewChild(AlertComponent, {static: true}) private alert: AlertComponent;
  spinner = false;
  showAddToCartButton = true;
  token = '';

  constructor(private restService: RestService, private router: Router) {
    ClarityIcons.addIcons(trashIcon);
  }

  ngOnInit(): void {
    this.refresh();
    this.token = '';
  }

  refresh(): void {
    this.getCartItems();
    this.getItemCount();
  }

  getCartItems(): void {
    const username = sessionStorage.getItem('user');
    this.restService.getCartItems(username).subscribe(data => {
      this.items = data;
      if (this.items.length > 0) {
        this.showAddToCartButton = false;
      }
    });
  }

  getItemCount(): void {
    this.restService.getFreeItemCount().subscribe(data => {
      this.itemCount = data;
      if (this.itemCount === 0) {
        this.showAddToCartButton = false;
      }
    });
  }

  addToCart(): void {
    const username = sessionStorage.getItem('user');
    this.showAddToCartButton = false;
    this.spinner = true;
    this.restService.addCartItem(username)
      .subscribe(data => {
        if (data) {
          this.token = data.token;
          this.alert.showSuccess('In Queue!');
        } else {
          this.alert.showError('Failed to enter Queue!');
        }
        this.checkIfComplete();
      });
  }

  checkIfComplete(): void {
    this.restService.getAuditToken(this.token)
      .subscribe(data => {
          if (data) {
            if (data.type === 'SUCCESS') {
              this.alert.showSuccess(data.message);
            } else {
              this.alert.showError(data.message);
            }
            this.refresh();
            this.spinner = false;
          }
        },
        error => {
          setTimeout(
            () => {
              this.checkIfComplete();
            },
            5000
          );
        });
  }

  deleteCartFor(id: any): void {
    const username = sessionStorage.getItem('user');
    this.restService.deleteCartItem(username, id)
      .subscribe(data => {
        if (data) {
          this.items = [];
          this.alert.showSuccess('Deleted from cart!');
        } else {
          this.alert.showError('Failed to delete from cart!');
        }
        this.refresh();
      });
  }

}
