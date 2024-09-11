package com.my.shared_data.lib

import com.my.shared_data.schema.Updates._

object OrderedOps {

  object implicits {

    implicit val dmUpdateOrdering: Ordering[DMUpdate] =
      new Ordering[DMUpdate] {

        def compare(x: DMUpdate, y: DMUpdate): Int = {
          def order(todo: DMUpdate): Int = todo match {
            case _: RegisterUser   => 0
            case _: CreateDataRequest => 1
            case _: CreateProviderProposal   => 2
            case _: ApproveProposal   => 3
            case _: SubmitData   => 5
          }

          order(x) compareTo order(y)
        }
      }
  }
}
