//
//  UICommunicator.swift
//  raa-ios-player
//
//  Created by Hamid on 2/2/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation

class UICommunicator : NSObject {
    
    private var listeners: [ModelCommunicator]! = []

    func registerEventListener(listenerObject: ModelCommunicator) {
        listeners.append(listenerObject)
    }
    
    func deregisterEventListener(listenerObject: ModelCommunicator) {
        for (index, listener) in listeners.enumerated() {
            if (listenerObject.hashCode() == listener.hashCode()) {
                listeners.remove(at: index)
                break
            }
        }
    }
    
    func notifyModelUpdate() {
        for listener in listeners {
            listener.modelUpdated(data: pullData())
        }
    }

    func pullData() -> Any? { return nil }
}

protocol ModelCommunicator {
    func modelUpdated(data: Any?)
    func hashCode() -> Int
}
