//
//  UICommunicator.swift
//  raa-ios-player
//
//  Created by Hamid on 2/2/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class UICommunicator {
    
    var uiListener: ModelCommunicator?

    func registerEventListener(listenerObject: ModelCommunicator) {
        uiListener = listenerObject
    }
    
    func notifyModelUpdate() {
        uiListener?.modelUpdated(data: pullData())
    }

    func pullData() -> Any? { return nil }
}

protocol ModelCommunicator {
    func modelUpdated(data: Any?)
}
