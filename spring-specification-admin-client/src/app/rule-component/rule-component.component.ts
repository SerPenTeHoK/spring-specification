import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';

import {NodeMovedEvent, TreeNode} from '../tree/tree-node';
import {RuleComponentService} from './rule-component.service';
import {convert, RuleComponentDataTreeNode} from './rule-component-tree-node';
import {RuleComponent} from './rule-component';
import {BusinessRule} from '../business-rule/business-rule';
import {Rule} from '../rule/rule';
import {RuleService} from '../rule/rule.service';

@Component({
    selector: 'p62-rule-component-update',
    template: `
        <h2 mat-dialog-title>Rule</h2>
        <mat-dialog-content>
            <form #form="ngForm">
                <mat-form-field>
                    <input matInput placeholder="Key" [(ngModel)]="ruleComponent.key" name="key" required [matAutocomplete]="keys">
                    <mat-autocomplete #keys="matAutocomplete">
                        <mat-option *ngFor="let rule of availableRules" [value]="rule.key">{{rule.name}}</mat-option>
                    </mat-autocomplete>
                </mat-form-field>
                <br>
                <mat-form-field>
                    <input matInput placeholder="Description" [(ngModel)]="ruleComponent.description" name="description">
                </mat-form-field>
            </form>
        </mat-dialog-content>
        <mat-dialog-actions>
            <button mat-button (click)="dialogRef.close(undefined)">Cancel</button>
            <button mat-button [disabled]="!form.form.valid" (click)="dialogRef.close(ruleComponent)">Apply</button>
        </mat-dialog-actions>`
})
export class EditRuleComponentDialog {

    ruleComponent: RuleComponent;
    availableRules: Rule[];

    constructor(public dialogRef: MatDialogRef<EditRuleComponentDialog>,
                @Inject(MAT_DIALOG_DATA) public data: any,
                ruleService: RuleService) {
        // Dialog data
        let businessRuleArgumentType: string = data.businessRuleArgumentType;
        this.ruleComponent = <RuleComponent> (data && data.ruleComponent || {}); // update or create

        ruleService.getAssociableRules(businessRuleArgumentType).subscribe(rs =>
            this.availableRules = rs
        );
    }

}

@Component({
    selector: 'p62-rule-component-delete',
    template: `
        <h2 mat-dialog-title>Delete</h2>
        <mat-dialog-content>
            Delete this rule?
        </mat-dialog-content>
        <mat-dialog-actions>
            <button mat-button (click)="dialogRef.close(false)">Cancel</button>
            <button mat-button (click)="dialogRef.close(true)">Discard</button>
        </mat-dialog-actions>`
})
export class DeleteRuleComponentDialog {

    constructor(public dialogRef: MatDialogRef<DeleteRuleComponentDialog>) {
    }

}

@Component({
    selector: 'p62-rule-component-settings',
    template: `
        <h2 mat-dialog-title>Settings</h2>
        <mat-dialog-content>
            <h3>Parameters</h3>
            <p62-parameter [ruleComponent]="ruleComponent"></p62-parameter>
            <br><!--fix-->
        </mat-dialog-content>`
})
export class SettingsRuleComponentDialog {

    ruleComponent: RuleComponent;

    constructor(protected dialogRef: MatDialogRef<SettingsRuleComponentDialog>,
                @Inject(MAT_DIALOG_DATA) public data: any) {
        this.ruleComponent = <RuleComponent> data;
    }

}

@Component({
    selector: 'p62-rule-component',
    template: `
        <p62-tree [value]="treeNode" (nodeMoved)="ruleMoved($event)">
            <ng-template #label let-node>
                <code>#{{node.data.ruleComponent.id}}</code> - <!--TODO test-->
                <b>{{ruleService.getFromKey(node.data.ruleComponent.key)?.name}}</b>
                <small>{{node.data.ruleComponent.description}}</small>
            </ng-template>
            <ng-template #options let-node>
                <div style="display: inline-flex;">
                    <!-- Composite: accept sub-rules -->
                    <div style="width: 40px;">
                        <ng-template
                                [ngIf]="['fr.pinguet62.springspecification.core.api.AndRule', 'fr.pinguet62.springspecification.core.api.OrRule', 'fr.pinguet62.springspecification.core.api.NotRule'].includes(node.data.ruleComponent.key)">
                            <button mat-icon-button (click)="openCreateDialog(node.data.ruleComponent)">
                                <mat-icon>add</mat-icon>
                            </button>
                        </ng-template>
                    </div>
                    <button mat-icon-button (click)="openUpdateDialog(node.data.ruleComponent)">
                        <mat-icon>mode_edit</mat-icon>
                    </button>
                    <!-- Custom: accept parameters -->
                    <div style="width: 40px;">
                        <ng-template
                                [ngIf]="!['fr.pinguet62.springspecification.core.api.AndRule', 'fr.pinguet62.springspecification.core.api.OrRule', 'fr.pinguet62.springspecification.core.api.NotRule'].includes(node.data.ruleComponent.key)">
                            <button mat-icon-button (click)="openSettingsDialog(node.data.ruleComponent)">
                                <mat-icon>settings</mat-icon>
                            </button>
                        </ng-template>
                    </div>
                    <button mat-icon-button (click)="openDeleteDialog(node.data.ruleComponent)">
                        <mat-icon>delete</mat-icon>
                    </button>
                </div>
            </ng-template>
        </p62-tree>`,
    styles: [
        '::ng-deep .ui-tree .ui-treenode-label { vertical-align: top !important; }', // vertical align because of element with different height
        '::ng-deep .ui-tree { width: 100%; }', // all screen width
        '::ng-deep .ui-tree .ui-treenode-children { padding: 0 0 0 5em; }' // sub-nodes indentation size
    ]
})
export class RuleComponentComponent implements OnInit {

    @Input()
    businessRule: BusinessRule;

    @Input()
    ruleComponent: RuleComponent;

    treeNode: TreeNode<RuleComponentDataTreeNode>;

    constructor(private dialog: MatDialog,
                private ruleComponentService: RuleComponentService,
                public ruleService: RuleService) {
    }

    ngOnInit(): void {
        this.refresh();
    }

    refresh(): void {
        this.ruleComponentService.get(this.ruleComponent.id).subscribe(r =>
            this.treeNode = convert(r)
        );
    }

    ruleMoved(event: NodeMovedEvent<RuleComponentDataTreeNode>): void {
        let ruleComponent: RuleComponent = {
            id: event.node.data.ruleComponent.id,
            parent: event.parent.data.ruleComponent.id,
            index: event.index
        };
        this.ruleComponentService.update(ruleComponent).subscribe(x =>
            this.refresh()
        );
    }

    openCreateDialog(parentRuleComponent: RuleComponent): void {
        let dialogConfig: MatDialogConfig = this.getCommonDialogConfig();
        dialogConfig.data = {
            businessRuleArgumentType: this.businessRule.argumentType,
            ruleComponent: null
        };
        let createDialog: MatDialogRef<EditRuleComponentDialog> = this.dialog.open(EditRuleComponentDialog, dialogConfig);
        createDialog.afterClosed().subscribe((createdRuleComponent: RuleComponent) => {
            // Canceled
            if (createdRuleComponent == null)
                return;

            createdRuleComponent.parent = parentRuleComponent.id;
            this.ruleComponentService.create(createdRuleComponent).subscribe(x =>
                this.refresh()
            );
        });
    }

    openUpdateDialog(ruleComponent: RuleComponent): void {
        let dialogConfig: MatDialogConfig = this.getCommonDialogConfig();
        dialogConfig.data = {
            businessRuleArgumentType: this.businessRule.argumentType,
            ruleComponent: Object.assign({}, ruleComponent)
        };
        let updateDialog: MatDialogRef<EditRuleComponentDialog> = this.dialog.open(EditRuleComponentDialog, dialogConfig);
        updateDialog.afterClosed().subscribe((updatedRule: RuleComponent) => {
            // Canceled
            if (updatedRule == null)
                return;

            this.ruleComponentService.update(updatedRule).subscribe(x =>
                this.refresh()
            );
        });
    }

    openSettingsDialog(ruleComponent: RuleComponent): void {
        let dialogConfig: MatDialogConfig = this.getCommonDialogConfig();
        dialogConfig.data = Object.assign({}, ruleComponent);
        dialogConfig.disableClose = false; // override
        //dialogConfig.width = '800px'; // custom
        let settingsDialog: MatDialogRef<SettingsRuleComponentDialog> = this.dialog.open(SettingsRuleComponentDialog, dialogConfig);
    }

    openDeleteDialog(ruleComponent: RuleComponent): void {
        let deleteDialog: MatDialogRef<DeleteRuleComponentDialog> = this.dialog.open(DeleteRuleComponentDialog, this.getCommonDialogConfig());
        deleteDialog.afterClosed().subscribe((confirm: boolean) => {
            if (confirm)
                this.ruleComponentService.delete(ruleComponent).subscribe(x =>
                    this.refresh()
                );
        });
    }

    getCommonDialogConfig(): MatDialogConfig {
        return {disableClose: true};
    }

}
