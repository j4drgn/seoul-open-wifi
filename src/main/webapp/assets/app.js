(function () {
  // index.jsp에 있는 input[name=lat], input[name=lnt] 자동 채우기
  function fillLatLnt(lat, lnt) {
    var latInput = document.querySelector('input[name="lat"]');
    var lntInput = document.querySelector('input[name="lnt"]');
    if (latInput) {
      latInput.value = lat;
    }
    if (lntInput) {
      lntInput.value = lnt;
    }
  }

  // 내 위치 가져오기 버튼
  var btn = document.getElementById("btn-my-location");
  if (btn) {
    btn.addEventListener("click", function () {
      // 디버그: 보안 컨텍스트(HTTPS/localhost) 여부 확인
      if (!window.isSecureContext) {
        alert("위치 기능은 HTTPS 또는 localhost에서만 동작합니다. (지금: " + location.origin
            + ")");
        return;
      }

      console.log("[geo] request start", {origin: location.origin});

      if (!navigator.geolocation) {
        alert("이 브라우저는 위치 기능을 지원하지 않습니다.");
        return;
      }
      btn.disabled = true;
      btn.textContent = "가져오는 중...";

      navigator.geolocation.getCurrentPosition(
          function (pos) {
            var lat = pos.coords.latitude.toFixed(6);
            var lnt = pos.coords.longitude.toFixed(6);
            console.log("[geo] success", {lat: lat, lnt: lnt});
            fillLatLnt(lat, lnt);

            btn.disabled = false;
            btn.textContent = "내 위치 가져오기";
          },
          function (err) {
            console.warn("[geo] error", err);

            var msg = "위치 정보를 가져오지 못했습니다.";
            if (err && err.code === 1) {
              msg += " (권한 거부)";
            } else if (err && err.code === 2) {
              msg += " (위치 확인 불가)";
            } else if (err && err.code === 3) {
              msg += " (시간 초과)";
            }

            // 권한/환경 안내를 함께 표시
            msg += "\n\n체크: 1) 브라우저 위치 권한 허용  2) HTTPS 또는 localhost  3) macOS 위치 서비스/브라우저 권한";

            alert(msg);
            btn.disabled = false;
            btn.textContent = "내 위치 가져오기";
          },
          {enableHighAccuracy: true, timeout: 8000}
      );
    });
  }
})();

document.getElementById("btn-load-wifi").addEventListener("click",
    function (e) {
      e.preventDefault(); // 페이지 이동 막기

      fetch("<%=request.getContextPath()%>/load-wifi")
      .then(res => res.json())
      .then(data => {
        alert("총 " + data.saved + "개 저장 완료");
      })
      .catch(err => {
        alert("OpenAPI 불러오기 실패");
        console.error(err);
      });
    });