nhn.husky.SE_TimeStamper = jindo.$Class({

    name : "SE_TimeStamper",

    $init : function(elAppContainer){

        this._assignHTMLObjects(elAppContainer);

    },

 

    _assignHTMLObjects : function(elAppContainer){

        this.oDropdownLayer =

                cssquery.getSingle("DIV.husky_seditor_TimeStamper_layer", elAppContainer);

        //div 레이어안에 있는 input button을 cssquery로 찾는 부분.

        this.oInputButton = cssquery.getSingle(".se_button_time", elAppContainer);

    },

 

    $ON_MSG_APP_READY : function(){

        this.oApp.exec("REGISTER_UI_EVENT",

                ["TimeStamper", "click", "SE_TOGGLE_TIMESTAMPER_LAYER"]);

        //input button에 click 이벤트를 할당.

        this.oApp.registerBrowserEvent(this.oInputButton, 'click','PASTE_NOW_DATE');

    },

 

    $ON_SE_TOGGLE_TIMESTAMPER_LAYER : function(){

        this.oApp.exec("TOGGLE_TOOLBAR_ACTIVE_LAYER", [this.oDropdownLayer]);

    },

 

    $ON_PASTE_NOW_DATE : function(){

        this.oApp.exec("PASTE_HTML", [new Date()]);

    }

});