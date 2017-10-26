$(document).ready(function() {
	var user_id = '1111';
	var user_name = 'User';
	var lat = 40.77;
	var lon = -74.14;
	document.getElementById('welcomeMsg').innerHTML = 'Welcome, ' + user_name;
	
	initGeoLocation();
	
	function initGeoLocation() {
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(onPositionUpload, onLoadPositionFailed, {
				maximumAge: 60000
			});
			showLoadingMessage('Retrieving location...');
		} else {
			onLoadPositionFailed();
		}
	}
	
	function onPositionUpload(position) {
		lat = position.coords.latitude;
		lon = position.coords.longitude;

		nearbyBtnClick();
	}
	
	function onLoadPositionFailed() {
		console.warn('navigator.geolocation is not available.');
		getLocationFromIP();
	}
	
	function getLocationFromIP() {
		$.ajax({
			type: 'GET',
			url: "http://ipinfo.io/json",
			success: function(response) {
				if ('loc' in response) {
					var loc = response.loc.split(', ');
					lat = loc[0];
					lon = loc[1];
				} else {
					console.warn('Failed to get location by IP.');
				}
				nearbyBtnClick(e);
			},
			error: function(response) {
				showErrorMessage('Failed to retrive location.');
			}
		})
	}
	
	function activeBtn(btnId) {
		var btns = document.getElementsByClassName('main-nav-btn');
		
		for (var i = 0; i < btns.length; i++) {
			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
		}
		
		var btn = document.getElementById(btnId);
		btn.className += ' active';
	}

	$('#nearbyBtn').click(nearbyBtnClick);
	function nearbyBtnClick() {
		/*e.preventDefault();*/
		console.log('load nearby restaurants.');
		activeBtn('nearbyBtn');
		
		$.ajax({
			type: 'GET',
			url: "./search" + "?user_id=" + user_id + '&lat=' + lat + '&lon=' + lon,
			success: function(response) {
				if (!response || response.length === 0) {
					showWarningMessage("No nearby restaurant.");
				} else {
					listItems(response);
				}
			},
			error: function(response) {
				showErrorMessage('Cannot load nearby restaurants.');
			}
		})		
	}
	
	$('#favBtn').click(function(e) {
		e.preventDefault();
		console.log('load favorite restaurants.');
		activeBtn('favBtn');
		
		$.ajax({
			type: 'GET',
			url: "./history" + "?user_id=" + user_id,
			success: function(response) {
				if (!response || response.length === 0) {
					showWarningMessage("No favorite restaurant.");
				} else {
					listItems(response);
				}
			},
			error: function(response) {
				showErrorMessage('Cannot load favorite restaurants.');
			}
		})
	});

	$('#recommendBtn').click(function(e) {
		e.preventDefault();
		console.log('load recommended restaurants.');
		activeBtn('recommendBtn');
		
		$.ajax({
			type: 'GET',
			url: "./recommend" + "?user_id=" + user_id + '&lat=' + lat + '&lon=' + lon,
			success: function(response) {
				if (!response || response.length === 0) {
					showWarningMessage("No recommended restaurant.");
				} else {					
					listItems(response);									
				}
			},
			error: function(response) {
				showErrorMessage('Cannot load recommended restaurants.');
			}
		})
	});
	
	function showWarningMessage(msg) {
		$('item-list').innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' + msg + '</p>';
	}

	function showErrorMessage(msg) {
		$('item-list').innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' + msg + '</p>';
	}
	
	function showLoadingMessage(msg) {
		$('item-list').innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' + msg + '</p>';
	}

	function listItems(items) {
		var itemList = document.getElementById('item-list');
		itemList.innerHTML = '';
		// TODO: temporarily display only 20. Implement: scroll down to display more.
		for (var i = 0; i < Math.min(items.length, 20); i++) {
		/*for (var i = 0; i < items.length; i++) {*/
			addItem(itemList, items[i]);
		}	
	}

	function addItem(itemList, item){
		var li = document.createElement("li");
		li.id = 'item-' + item.item_id;
		li.className = 'item';
		li.dataset.item_id = item.item_id;
		li.dataset.favorite = item.favorite;
		
		var img = document.createElement("img");
		if (item.image_url) {	
			img.src = item.image_url;				
		} else {
			img.src = "http://screenwerk.com/wpn/media/Screen-Shot-2013-02-19-at-7.06.02-AM.png"; 
		}
		li.appendChild(img);
		
		var section = document.createElement("div");
		var title = document.createElement("a");
		title.href = item.url;
		title.target = '_blank';
		title.className = 'item-name';
		title.innerHTML = item.name;
		section.appendChild(title);
		
		var category = document.createElement("p");
		category.className = 'item-category';
		category.innerHTML = 'Category: ' + item.categories.join(', ');
		section.appendChild(category);
		
		var stars = document.createElement("div");
		stars.className = 'stars';
		for (var i = 0; i < Math.floor(item.rating); i++) {
			var star = document.createElement("i");
			star.className = 'fa fa-star';
			stars.appendChild(star);
		}
		if (('' + item.rating).match(/\.5$/)) {
			var halfStar = document.createElement("i");
			halfStar.className = 'fa fa-star-half-o';
			stars.appendChild(halfStar);
		}	
		section.appendChild(stars);
		
		li.appendChild(section);
		
		var address = document.createElement("p");
		address.className = 'item-address';
		address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g,'');	
		li.appendChild(address);
		
		var favLink = document.createElement("p");
		favLink.className = 'fav-link';
		favLink.onclick = function() {		
			changeFavoriteItem(item.item_id);
		}
		
		var favIcon = document.createElement("i");
		favIcon.id = 'fav-icon-' + item.item_id;
		favIcon.className = item.favorite ? 'fa fa-heart' : 'fa fa-heart-o';
		favLink.appendChild(favIcon);
		li.appendChild(favLink);
			
		itemList.appendChild(li);
	}

	function changeFavoriteItem(item_id) {
		var li = document.getElementById('item-' + item_id);
		var favIcon = $('fav-icon-' + item_id);
		var favorite = li.dataset.favorite !== 'true';
		
		var url = './history';
		var req = JSON.stringify({
			user_id: user_id,
			favorite: [item_id]
		});
		var method = favorite ? 'POST' : 'DELETE';
		
		$.ajax({
			type: method,
			url: url,
			data: req,
			dataType: 'json',
			success: function(res) {
				if (res.result === 'SUCCESS') {
					li.dataset.favorite = favorite;
					favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
				}
			},
			error: function() {
				console.log("Error to set/unset favorites");
			}
		})
	}
});

