/**
 * Created by Administrator on 2017/9/5.
 */
layui.use(['element','form'], function(){
	var element = layui.element;
	var form = layui.form;
	var $ = layui.$;
	var sid = [[${status.id}]];
	$('*[name=loc]').click(function () {
		alert(sid);
		var cid = $(this).attr("value");
		var select_url = "/add_travel_city";
		var unselect_url = "/unselect_city";
		var data = {sid:sid,cid:cid};
		$.ajax({
			type : "post",
			async : false,  //同步请求
			url : select_url,
			data : data,
			timeout:1000,
			success:function(dates){
				alert(dates);
				$("#select").html(dates);//要刷新的div
			},
			error: function() {
				// alert("失败，请稍后再试！");
			}
		});
		$.ajax({
			type : "post",
			async : false,  //同步请求
			url : unselect_url,
			data : data,
			timeout:1000,
			success:function(dates){
				alert(dates);
				$("#unselect").html(dates);//要刷新的div
			},
			error: function() {
				// alert("失败，请稍后再试！");
			}
		});
	});
	$('*[name=del_city]').click(function () {
		var cid = $(this).attr("value");
		alert(cid);
		var select_url = "/del_travel_city";
		var unselect_url = "/unselect_city";
		var data = {sid:sid,cid:cid};
		$.ajax({
			type : "post",
			async : false,  //同步请求
			url : select_url,
			data : data,
			timeout:1000,
			success:function(dates){
				alert(dates);
				$("#select").html(dates);//要刷新的div
			},
			error: function() {
				// alert("失败，请稍后再试！");
			}
		});
		$.ajax({
			type : "post",
			async : false,  //同步请求
			url : unselect_url,
			data : data,
			timeout:1000,
			success:function(dates){
				alert(dates);
				$("#unselect").html(dates);//要刷新的div
			},
			error: function() {
				// alert("失败，请稍后再试！");
			}
		});
	});
});
	
