package com.utn.haveiseenit.activities.users.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import com.utn.haveiseenit.R
import com.utn.haveiseenit.database.UserDao
import com.utn.haveiseenit.database.appDatabase
import com.utn.haveiseenit.forms.Form
import com.utn.haveiseenit.services.UserAuthentication

class CreateAccountFragment : Fragment() {
    private lateinit var v: View
    private lateinit var newAccountForm: Form
    private var db: appDatabase? = null
    private var userDao: UserDao? = null
    private lateinit var authService: UserAuthentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_create_account, container, false)
        return v
    }

    override fun onStart() {
        super.onStart()
        db = appDatabase.getAppDataBase(v.context)
        authService = UserAuthentication(db!!.userDao())
        setLinkToLogin()
        setArrowBack()
        newAccountForm = Form(requireContext())
            .addTextInput(
                v.findViewById(R.id.create_account_username),
                "userName",
                errorMessage = getString(R.string.username_missing_message)
            )
            .addEmailInput(v.findViewById(R.id.create_account_email), "email")
            .addPasswordInput(v.findViewById(R.id.create_account_password), "password")
            .addPasswordInput(
                v.findViewById(R.id.create_account_repeat_password),
                "repeatPassword",
                isLast = true
            ) {
                if (newAccountForm.getValue("password") != newAccountForm.getValue("repeatPassword")) {
                    newAccountForm.markInputInvalid(
                        "repeatPassword",
                        getString(R.string.password_mismatch_message)
                    )
                }
            }.addSubmitButton(v.findViewById(R.id.create_account_submit_button), onSubmit)
    }

    private val onSubmit = {
        if (newAccountForm.isValid()) {
            authService.createUser(
                newAccountForm.getValue("userName"),
                newAccountForm.getValue("email"),
                newAccountForm.getValue("password")
            ) { error ->
                if (error == null) {
                    Snackbar.make(
                        v,
                        getString(R.string.user_created_message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    navigateToLogin()
                }
            }
        }
    }

    private fun setLinkToLogin() {
        v.findViewById<TextView>(R.id.create_account_to_login_link).setOnClickListener {
            navigateToLogin()
        }
    }

    private fun setArrowBack() {
        v.findViewById<FloatingActionButton>(R.id.create_account_arrow_back).setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        v.findNavController()
            .navigate(CreateAccountFragmentDirections.actionCreateAccountFragmentToLogin())
    }
}

