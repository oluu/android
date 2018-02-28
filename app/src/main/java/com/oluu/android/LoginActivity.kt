package com.oluu.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.regions.Regions
import kotlinx.android.synthetic.main.activity_login.*

//TODO: move these later... shouldn't be here
const val USER_POOL_ID = "us-east-1_8jXAyv3iW"
const val CLIENT_ID = "6ci3qn66kp1pnjonkdmk19smet"
const val CLIENT_SECRET = "1bqm3d6u7akafnaveid24atgcpfkbb7d34ec7a31n7ob7o28s6qf"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // cognito setup
        val userPool = CognitoUserPool(baseContext, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, Regions.US_EAST_1)
        val userAttributes = CognitoUserAttributes()

        /************************************
         *** Cognito Callbacks start here ***
         ************************************/

        // signup callback
        val signupCallback: SignUpHandler = object: SignUpHandler {
            override fun onSuccess(user: CognitoUser?, userConfirmed: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
                if (!userConfirmed) {
                    //TODO: user must be confirmed first
                } else {
                    //TODO: login the user probably
                }
            }
            override fun onFailure(exception: Exception) {
                System.out.println(exception.toString())
                //TODO: Sign up failed, code check the exception for cause and perform remedial actions.
            }
        }
        // confirm user callback
        val confirmCallback: GenericHandler = object: GenericHandler {
            override fun onSuccess() {
                //TODO: User was successfully confirmed
            }
            override fun onFailure(exception: Exception) {
                //TODO: User confirmation failed. Check exception for the cause.
            }
        }
        // login callback
        val authenticationHandler: AuthenticationHandler = object: AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                //TODO: login was successful, cognitoUserSession will contain tokens for the user
            }
            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
                //TODO: The API needs user login credentials to continue
                // val authenticationDetails = AuthenticationDetails(userId, password, null)
                //TODO: Pass the user login credentials to the continuation
                // authenticationContinuation.setAuthenticationDetails(authenticationDetails)
                //TODO: Allow the login to continue
                // authenticationContinuation.continueTask()
            }
            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                // not doing MFA so do nothing here
            }
            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                //TODO: look into this
            }
            override fun onFailure(exception: Exception) {
                //TODO: handle login error
            }
        }
        // get user info from user pool
        val getDetailsHandler: GetDetailsHandler = object: GetDetailsHandler {
            override fun onSuccess(cognitoUserDetails:CognitoUserDetails) {
                //TODO: The user detail are in cognitoUserDetails
            }
            override fun onFailure(exception:Exception) {
                //TODO: Fetch user details failed, check exception for the cause
            }
        }

        /**********************************
         *** Cognito Callbacks end here ***
         **********************************/

        // sign up button retrieves text and invokes the signup callback
        signupBtn.setOnClickListener {
            userAttributes.addAttribute("email", emailText.text.toString())
            userPool.signUpInBackground(emailText.text.toString(), passwordText.text.toString(), userAttributes, null, signupCallback)
        }
    }

}
