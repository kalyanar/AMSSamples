<%@include file="/libs/foundation/global.jsp" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <%
        String suffix = slingRequest.getRequestPathInfo().getSuffix();
        String logurl = "ws://"+slingRequest.getServerName()+":3333/ws"+suffix;
    %>
    <title>AMS Tools - Log Tail</title>

</head>
<body>
<input type="button" id="start" value="tail"/>
<input type="button" id="stop" value="stop"/>
<input type="button" id="clear" value="clear"/>
<label>filter only lines containing</label><input type="text" id="filter" style="width:300px">....
<label>highlight the line containing text:</label><input type="text" id="highlight" style="width:300px">  <br/>
<div style='border: 1px solid; padding: 5px; height: 780px; overflow: scroll;'>
<code contenteditable="true"  id='log'></code>
    </div>
</body>
<cq:includeClientLib js="coralui"/>
<script>
    function log(msg) {
        var log = $('#log');

        log.append(msg + "<br/>")
                //.scrollTop(log[0].scrollHeight - log.height());
    }
    function clear() {
        var log = $('#log');
        
        log.html("");
        //.scrollTop(log[0].scrollHeight - log.height());
    }

    $(function () {
        var ws = null;
        $("#clear").click(function() {
        clear();
        });
        $("#start").click(function(){

            if (ws == null || ws.readyState != 1) {
                ws = new WebSocket("<%=logurl%>");
                ws.onerror = function (e) {
                    log('Error : ' + e.message)
                }

                ws.onopen = function () {

                    log('connected')
                }
                ws.onclose = function () {
                    log('disconnected')
                }

                ws.onmessage = function (d) {
                    var data = d.data;
                    var text = $("#highlight").val();
                    var filter = $("#filter").val();
                    if(filter!=""){
                        if(data.indexOf(filter)==-1){
                        data ="";
                        }
                    }
                   if(text!=""){
                       if(data.indexOf(text)!=-1){
                           data="<span style='background-color: black;color:white;font-size: large'>"+data+"</span>";
                       }
                   }

                    log( data);
                }
            }else{

                ws.close();
            }
        });

        $("#stop").click(function(){
            log('closing connection');
            ws.close();
        });

    });

</script>
</html>