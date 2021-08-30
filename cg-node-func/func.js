const fdk=require('@fnproject/fdk');
const https = require('https')
const axios = require('axios');
const CryptoJS = require('crypto-js')

fdk.handle(async function(payload){

  var postData = JSON.stringify({"userId":process.env.USER_ID,"messagePayload":{"type":"application","payloadType":"problemMsg","channelName":"Slack","variables":{"tr_sender":"Notification","tr_compartmentName":payload.data.compartmentId,"tr_resourceName": payload.data.resourceName,"tr_eventTime":payload.eventTime,"tr_resourceId":payload.data.resourceId,"tr_riskLevel":payload.data.additionalDetails.riskLevel,"tr_problemDescription":payload.data.additionalDetails.problemDescription},"channelProperties":{"teamId":process.env.TEAM_ID,"channel":process.env.CHANNEL_ID}}});

  var config = {
    method: 'post',
    url: process.env.ODA_HTTPS_URL,
    headers: { 
      'Content-Type': 'application/json', 
      'X-Hub-Signature': 'sha256='+CryptoJS.HmacSHA256(postData, process.env.SECRET)
    },
    data : postData
  };
  
  await axios(config)
  .then(function (response) {
    console.log(postData);
  })
  .catch(function (error) {
    console.log(error);
  });

  return "Successfully inovoke function ";
})
