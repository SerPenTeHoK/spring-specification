import {Component} from "@angular/core";
import {RuleService} from "./rule/rule.service";
import {Rule} from "./rule/rule";

@Component({
    selector: 'app-root',
    template: `
		<md-toolbar color="primary">
			<button (click)="sidenav.toggle()" md-icon-button>
                <md-icon>menu</md-icon>
            </button>
			
			<span>Admin</span>
		</md-toolbar>
		
		<md-sidenav-container>
			<md-sidenav #sidenav mode="side" [opened]="true">
				<md-nav-list>
					<md-list-item>
						<md-icon md-list-icon>settings</md-icon>
						<span md-line>Users</span>
					</md-list-item>
				</md-nav-list>
			</md-sidenav>
			
			<p62-rule *ngFor="let rule of rules" [rule]="rule"></p62-rule>
			
		</md-sidenav-container>`
})
export class AppComponent {
    rules: Rule[];

    constructor(private ruleService: RuleService) {
        this.ruleService.getAllRoots().subscribe(rs =>
            this.rules = rs
        );
    }
}