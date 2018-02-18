//
//  UICommunicator.swift
//  raa-ios-player
//
//  Created by Hamid on 2/2/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit

class UICommunicator<T> : NSObject {
    
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
    
    func notifyModelUpdate(data: T) {
        for listener in listeners {
            listener.modelUpdated(data: data)
        }
    }

    func pullData() -> Promise<T>? {
        assert(false, "This method must be overriden by the subclass")
        return nil
    }
}

protocol ModelCommunicator {
    func modelUpdated(data: Any?)
    func hashCode() -> Int
}
