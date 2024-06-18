<!DOCTYPE html>
<html>
<head>
   <style>
      .centered-div {
           width: 50%;
           height: 50%;
           border: 1px solid #000;
           text-align: center;
           background-color: #f0f0f0;
       }
   
      body {
          font-family: Arial, sans-serif;
          color: #333;
          margin: 0;
          padding: 0;
      }

      .container {
          max-width: 600px;
          margin: 200px auto;
          padding: 20px;
          background-color: #f5f5f5;
          border-radius: 5px;
          align-items: center;
      }
          
      .email {
          color: #0000FF;
          font-weight: bold;
      }

      h1 {
          color: #333;
          font-size: 24px;
          margin-bottom: 20px;
      }

      p {
          font-size: 16px;
          line-height: 24px;
          margin-bottom: 20px;
      }

      .custom-alert {
          display: none;
          position: fixed;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          background-color: white;
          padding: 20px;
          border: 2px solid #316fea;
          border-radius: 5px;
          box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
      }

      .custom-alert p {
          margin: 0;
      }

      .custom-alert .close-btn {
          margin-top: 20px;
          display: inline-block;
          padding: 10px 20px;
          background-color: #316fea;
          color: white;
          text-decoration: none;
          border-radius: 5px;
          cursor: pointer;
      }
   </style>
   <script>
       document.addEventListener("DOMContentLoaded", function() {
           var message = document.getElementById("Message").textContent;
           var email = document.getElementById("Email").textContent;
           showCustomAlert(message, email);
       });

       function showCustomAlert(message, email) {
           var customAlert = document.getElementById("customAlert");
           customAlert.innerHTML = "<p>" + message + " <span class='email'>" + email + "</span></p>" +
                                   "<a class='close-btn' onclick='closeCustomAlert()'>Close</a>";
           customAlert.style.display = "block";
       }

       function closeCustomAlert() {
           document.getElementById("customAlert").style.display = "none";
       }
   </script>
</head>
<body>
    <div class="container">
        <h1>
            <span id="Message">This is a message</span>
            <span class="email" id="Email">example@example.com</span>
        </h1> 
    </div>

    <div id="customAlert" class="custom-alert"></div>
</body>
</html>
