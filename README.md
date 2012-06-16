This is an example how to implement an TCP client for the
[sweet productivity](http://sweet-productivity.com/) server.
With this base class you can implement own groovy TCP services like this
[greeting service](https://github.com/sweetp/service-greeting-groovy-tcp),
but since groovy lives in the JVM it is recommended to build a jvm service.

If you want to run a TCP service, you have to place it in the "server/service-tcp/"
directory and add this this to your "server/services.json" file:

	{
		"id":"YOUR SERVICE",
		"exec":[
			"java",
			"-jar",
			"YOUR_SERVICE.jar"
		],
		"dir":"services-tcp/"
	}

This is a complete and valid services.json file as an example:

    [
        {
            "id":"greeting-groovy-tcp",
            "exec":[
                "java",
                "-jar",
                "greeting-groovy-tcp-0.1.jar"
            ],
            "dir":"services-tcp/"
        },
        {
            "id":"git",
            "clazz": "org.hoschi.sweetp.services.git.GitService"
        }
    ]

