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
              align-items : center;
              
          }
          
           .email.color {
            color: #0000FF;
           
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

          table {
              border-collapse: collapse;
              width: 100%;
          }

          th, td {
              padding: 10px;
              text-align: left;
              border-bottom: 1px solid #ddd;
          }

          th {
              background-color: #f2f2f2;
              font-weight: bold;
          }

          .button {
              color: #fff;
              display: inline-block;
              padding: 10px 20px;
              background-color: #316fea;
              text-decoration: none;
              border-radius: 4px;
              font-weight: bold;
              margin-right: 10px;
          }

              .button:hover {
              background-color: #1e4bb5;
          }

          .signature {
              margin-top: 45px;
              font-size: 16px;
          }

          .signature p {
              margin: 5px 0;
          }

          .support {
              font-size: 16px;
              margin-top: 20px;
          }

          .support a {
              color: #316fea;
              text-decoration: none;
          }
      </style>
</head>
<body>
    <div class="container" >
        <p>
         <h1>
            <span id="Message">${Message}</span>
            <span class="email color" id="Email">${Email}</span>
            </h1> 
        </p>
    </div>
</body>
</html>
