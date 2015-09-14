/**
 * PasswordCellEditor
 * A YUI DataTable extension
 * Originally made by Mihaly Koles, http://koles.hu, 2009.02.10.
 * Original version: http://koles.hu/sandbox/yui_datatable_password/YUI_DataTable_PasswordCellEditor.js
 */

var Ev = YAHOO.util.Event,
   lang = YAHOO.lang,
   widget = YAHOO.widget,
   ua = YAHOO.env.ua;
   
//Password field editor with two text inputs
widget.PasswordCellEditor = function(oConfigs) {
    this._sId = "yui-passwordceditor" + YAHOO.widget.BaseCellEditor._nCount++;
    widget.PasswordCellEditor.superclass.constructor.call(this, "textbox", oConfigs);
};

lang.extend(widget.PasswordCellEditor, widget.BaseCellEditor, {
    textbox0 : null,
    textbox1 : null,
    msgEl : null,
    renderForm : function() {
        var elTextbox0, elTextbox1, elForm;
        // Bug 1802582: SF3/Mac needs a form element wrapping the input
        if(ua.webkit>420) {
            elForm = this.getContainerEl().appendChild(document.createElement("form"))
        }
        else {
            elForm = this.getContainerEl();
        }
        elForm.style.textAlign="right";
        elForm.appendChild(document.createElement("label")).innerHTML="Password:";
        elTextbox0= document.createElement("input");
        elTextbox0.type = "password";
        elForm.appendChild(elTextbox0);
        elForm.appendChild(document.createElement("br"));
        elForm.appendChild(document.createElement("label")).innerHTML="Confirm:";
        elTextbox1= document.createElement("input");
        elTextbox1.type = "password";
        elForm.appendChild(elTextbox1);
        elForm.appendChild(document.createElement("br"));
        this.msgEl=elForm.appendChild(document.createElement("span"));

        this.textbox0 = elTextbox0;
        this.textbox1 = elTextbox1;

        // Save on enter by default
        // Bug: 1802582 Set up a listener on each textbox to track on keypress
        // since SF/OP can't preventDefault on keydown
        Ev.addListener(elTextbox0, "keyup", function(v){
            if (this.checkPasswords())
            {
                if((v.keyCode === 13)) {
                    // Prevent form submit
                    YAHOO.util.Event.preventDefault(v);
                    this.save();
                }
            }
        }, this, true);

        Ev.addListener(elTextbox1, "keyup", function(v){
            if (this.checkPasswords())
            {
                if((v.keyCode === 13)) {
                    // Prevent form submit
                    YAHOO.util.Event.preventDefault(v);
                    this.save();
                }
            }
        }, this, true);

        if(this.disableBtns) {
            // By default this is no-op since enter saves by default
            this.handleDisabledBtns();
        }
    },
    checkPasswords : function()
    {
        if (this.textbox0.value==null
            || this.textbox0.value==""
            || this.textbox1.value==null
            || this.textbox1.value=="")
        {
            this.msgEl.innerHTML="";
            return false;
        }
        else {
            if (this.textbox0.value == this.textbox1.value) {
                this.msgEl.innerHTML="New password is confirmed.";
                this.msgEl.style.color="#008000";
                return true;
            }
            else {
                this.msgEl.innerHTML="Password don't match!";
                this.msgEl.style.color="#800000";
                return false;
            }
        }
    },
    move : function() {
        this.textbox0.style.width = this.getTdEl().offsetWidth + "px";
        this.textbox1.style.width = this.getTdEl().offsetWidth + "px";
        widget.PasswordCellEditor.superclass.move.call(this);
    },
    resetForm : function() {
        this.textbox0.value = lang.isValue(this.value) ? this.value.toString() : "";
        this.textbox1.value = lang.isValue(this.value) ? this.value.toString() : "";
    },
    focus : function() {
        this.textbox0.focus();
        this.textbox0.select();
    },
    handleDisabledBtns : function() {
        this.subscribe("blurEvent", function(v){
            if (this.checkPasswords()) {
                // Save on blur
                this.save();
            }
        }, this, true);
    },
    getInputValue : function() {
        if (this.checkPasswords()) return this.textbox0.value;
        else return "";
    }
});

// Copy static members to PasswordCellEditor class
lang.augmentObject(widget.PasswordCellEditor, widget.BaseCellEditor);


//cell formatter for password fields
DataTableUtils={
   PasswordFormatter:function(el,oRecord,oColumn,oData){
      if (oData==null || oData=='') oData='xxxxxxxx';
      {
         el.innerHTML='';
         var oStr = new String(oData);
         for (var i=0; i<oStr.length; i++)
         el.innerHTML+="*";
      }
   }
}
