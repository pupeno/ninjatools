{:profiles/dev  {:env {:database-url        "jdbc:postgresql://localhost/ninjatools_dev"
                       :email-delivery-mode "smtp"
                       :email-url           "http://ninjatools.lvh.me:3000/"
                       :email-host          "smtp.sendgrid.net"
                       :email-user          "ninjatools"
                       :email-pass          "secret-token"
                       :yeller-token        "secret-token"}}
 :profiles/test {:env {:database-url        "jdbc:postgresql://localhost/ninjatools_test"
                       :email-delivery-mode "test"
                       :email-url           "http://ninjatools.lvh.me:3000/"}}}
