## Running instructions :-
1) Create a duplicate of the `.env.template` file in the root of the 
   project for each environment like dev,qa,prod etc and a `.env.sam` file for testing using SAM locally
2) Assign values to each env variable in the .env file
3) Run the `run.sh` and specify the environment with the -e flag. For eg:- `sh ci/run.sh -e dev`
4) To locally run using SAM, run the script with the flag -s : `sh ci/run.sh -s`

## OAuth token guide :-
Execute below command to get the token, export Auth0 credentials in current terminal beforehand

curl --request POST \
--url https://teenthofabud-dev.eu.auth0.com/oauth/token \
--header 'content-type: application/json' \
--data '{
"client_id":$CLIENT_ID,
"client_secret":$CLIENT_SECRET,
"audience":"toab-jitsi",
"grant_type":"client_credentials"
}'
