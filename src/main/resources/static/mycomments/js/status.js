layui.use(['element','carousel','layer'], function(){
	var carousel = layui.carousel;
	var $ = layui.$;
	//建造实例
	carousel.render({
		elem: '#test1'
		,width: '100%' //设置容器宽度
		,arrow: 'hover' //始终显示箭头
	});
	
	$(document)
		.ready(function() {
			alert(1)
		$('#newStatus').click(function () {
			var user_id = [[${user.id}]];
			var new_status = layer.open({
				id:'new_status',
				title: false,
				type: 2,
				content:'/new_status{user_id}',
				area: ['100%', '100%'],
				anim:6
			});
			layer.full(new_status);
		});
	});
	
});