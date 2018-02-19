//
//  Card.swift
//  Cards
//
//  Created by Paolo on 09/10/17.
//  Copyright © 2017 Apple. All rights reserved.
//

//TODO: - Risolvere il problema del layout dopo l' animazione passando il backgroundIV al detailVC
//TODO: - Trovare il frame originario relativo alla view del VC della card ( in una table )

import UIKit

@objc public protocol CardDelegate {
    
    @objc optional func cardDidTapInside(card: Card)
    @objc optional func cardWillShowDetailView(card: Card)
    @objc optional func cardDidShowDetailView(card: Card)
    @objc optional func cardWillCloseDetailView(card: Card)
    @objc optional func cardDidCloseDetailView(card: Card)
    @objc optional func cardIsShowingDetail(card: Card)
    @objc optional func cardIsHidingDetail(card: Card)
    @objc optional func cardDetailIsScrolling(card: Card)
    
    @objc optional func cardHighlightDidTapButton(card: CardHighlight, button: UIButton)
    @objc optional func cardPlayerDidPlay(card: CardPlayer)
    @objc optional func cardPlayerDidPause(card: CardPlayer)
}

@IBDesignable open class Card: UIView, CardDelegate {
    
    // Storyboard Inspectable vars
    /**
     Color for the card's labels.
     */
    @IBInspectable public var textColor: UIColor = UIColor.black
    /**
     Amount of blur for the card's shadow.
     */
    @IBInspectable public var shadowBlur: CGFloat = 14 {
        didSet{
            self.layer.shadowRadius = shadowBlur
        }
    }
    /**
     Alpha of the card's shadow.
     */
    @IBInspectable public var shadowOpacity: Float = 0.6 {
        didSet{
            self.layer.shadowOpacity = shadowOpacity
        }
    }
    /**
     Color of the card's shadow.
     */
    @IBInspectable public var shadowColor: UIColor = UIColor.gray {
        didSet{
            self.layer.shadowColor = shadowColor.cgColor
        }
    }
    /**
     The image to display in the background.
     */
    @IBInspectable public var backgroundImage: UIImage? {
        didSet{
            // Make room for the new image
            self.backgroundImageView.removeFromSuperview()
        }
    }
    /**
     Corner radius of the card.
     */
    @IBInspectable public var cardRadius: CGFloat = 20{
        didSet{
            self.layer.cornerRadius = cardRadius
        }
    }
    /**
     Insets between card's content and edges ( in percentage )
     */
    @IBInspectable public var contentInset: CGFloat = 3 {
        didSet {
            insets = LayoutHelper(rect: originalFrame).X(contentInset)
        }
    }
    /**
     Color of the card's background.
     */
    override open var backgroundColor: UIColor? {
        didSet(new) {
            if let color = new { backgroundIV.backgroundColor = color }
            if backgroundColor != UIColor.clear { backgroundColor = UIColor.clear }
        }
    }
    
    var contentViewController: UIViewController?
    /**
     contentViewController  -> The view controller to present when the card is tapped
     from                   -> Your current ViewController (self)
     */
    public func shouldPresent( _ contentViewController: UIViewController?, from superVC: UIViewController?, fullscreen: Bool = false) {
        if let content = contentViewController {
            self.superVC = superVC
            //detailVC.addChildViewController(content)
            self.contentViewController = content
            //detailVC.detailView = content.view
            detailVC.card = self
            detailVC.delegate = self.delegate
            detailVC.isFullscreen = fullscreen
        }
    }
    /**
     If the card should display parallax effect.
     */
    public var hasParallax: Bool = true {
        didSet {
            if self.motionEffects.isEmpty && hasParallax { goParallax() }
            else if !hasParallax && !motionEffects.isEmpty { motionEffects.removeAll() }
        }
    }
    /**
     Delegate for the card. Should extend your VC with CardDelegate.
     */
    public var delegate: CardDelegate?
    
    //Private Vars
    fileprivate var tap = UITapGestureRecognizer()
    fileprivate var detailVC = DetailViewController()

    var superVC: UIViewController?
    var originalFrame = CGRect.zero
    var backgroundIV = UIImageView()

    fileprivate var grayoutMask = UIView()
    
    var insets = CGFloat()
    var isPresenting = false
    
    var backgroundImageView = UIImageView()
    
    //MARK: - View Life Cycle
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialize()
    }
    
    func initialize() {
        // Tap gesture init
        self.addGestureRecognizer(tap)
        tap.delegate = self
        tap.cancelsTouchesInView = false
       
        detailVC.transitioningDelegate = self
        
        // Adding Subviews
        self.addSubview(backgroundIV)
        
        backgroundIV.isUserInteractionEnabled = true
        
        if backgroundIV.backgroundColor == nil {
            backgroundIV.backgroundColor = UIColor.white
            super.backgroundColor = UIColor.clear
        }
        
        self.addSubview(grayoutMask)
        self.bringSubview(toFront: grayoutMask)
        grayoutMask.alpha = 0.3
        grayoutMask.backgroundColor = UIColor.gray

        self.enable()
    }
    
    override open func draw(_ rect: CGRect) {
        super.draw(rect)
        originalFrame = rect
        
        self.layer.shadowOpacity = shadowOpacity
        self.layer.shadowColor = shadowColor.cgColor
        self.layer.shadowOffset = CGSize.zero
        self.layer.shadowRadius = shadowBlur
        self.layer.cornerRadius = cardRadius

        // Remove everything first
        backgroundImageView = UIImageView(frame: originalFrame)
        backgroundImageView.image = backgroundImage
        backgroundImageView.alpha = 0.7
        backgroundImageView.contentMode = .scaleAspectFit
        
        backgroundIV.addSubview(backgroundImageView)
        backgroundIV.sendSubview(toBack: backgroundImageView)

        backgroundIV.layer.cornerRadius = self.layer.cornerRadius
        backgroundIV.clipsToBounds = true
        
        backgroundIV.frame.origin = bounds.origin
        backgroundIV.frame.size = CGSize(width: bounds.width, height: bounds.height)
        contentInset = 6

        // gray layer is out there waiting
        grayoutMask.layer.cornerRadius = self.layer.cornerRadius
        grayoutMask.clipsToBounds = true

        grayoutMask.frame.origin = bounds.origin
        grayoutMask.frame.size = CGSize(width: bounds.width, height: bounds.height)
    }
    
    
    //MARK: - Layout
    
    func layout(animating: Bool = true) {        
        let gimme = LayoutHelper(rect: backgroundIV.bounds)

        let widthRatio = backgroundImageView.bounds.size.width / (backgroundImageView.image?.size.width)!
        let heightRatio = backgroundImageView.bounds.size.height / (backgroundImageView.image?.size.height)!
        let scale = min(widthRatio, heightRatio)
        let imageWidth = scale * (backgroundImageView.image?.size.width)!
        let imageHeight = scale * (backgroundImageView.image?.size.height)!
        backgroundImageView.frame = CGRect(x: 0, y: 0, width: imageWidth, height: imageHeight)
        
        backgroundImageView.frame.origin = CGPoint(x: gimme.RevX(0, width: backgroundImageView.bounds.width), y: 0)
    }
    
    
    //MARK: - Actions
    
    func disable() {
        backgroundIV.alpha = 0.1
        grayoutMask.isHidden = false
        tap.cancelsTouchesInView = true
    }
    
    func enable() {
        backgroundIV.alpha = 1.0
        grayoutMask.isHidden = true
        tap.cancelsTouchesInView = false
    }
    
    @objc func cardTapped() {
        self.delegate?.cardDidTapInside?(card: self)
        
        if let vc = superVC {
            // Add child VC
            if self.contentViewController != nil {
                self.detailVC.addChildViewController(self.contentViewController!)
                self.detailVC.detailView = self.contentViewController?.view
            }
            vc.present(self.detailVC, animated: true, completion: nil)
        } else {
            resetAnimated()
        }
    }


    //MARK: - Animations
    
    private func pushBackAnimated() {
        
        UIView.animate(withDuration: 0.2, animations: { self.transform = CGAffineTransform(scaleX: 0.95, y: 0.95) })
    }
    
    private func resetAnimated() {
        
        UIView.animate(withDuration: 0.2, animations: { self.transform = CGAffineTransform.identity })
    }
    
    func goParallax() {
        let amount = 20
        
        let horizontal = UIInterpolatingMotionEffect(keyPath: "center.x", type: .tiltAlongHorizontalAxis)
        horizontal.minimumRelativeValue = -amount
        horizontal.maximumRelativeValue = amount
        
        let vertical = UIInterpolatingMotionEffect(keyPath: "center.y", type: .tiltAlongVerticalAxis)
        vertical.minimumRelativeValue = -amount
        vertical.maximumRelativeValue = amount
        
        let group = UIMotionEffectGroup()
        group.motionEffects = [horizontal, vertical]
        self.addMotionEffect(group)
    }
    
}


    //MARK: - Transition Delegate

extension Card: UIViewControllerTransitioningDelegate {
    
    public func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return Animator(presenting: true, from: self)
    }
    
    public func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return Animator(presenting: false, from: self)
    }
    
}

    //MARK: - Gesture Delegate

extension Card: UIGestureRecognizerDelegate {
    
    open override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        cardTapped()
    }
    
    override open func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        
        if let superview = self.superview {
            originalFrame = superview.convert(self.frame, to: nil)
        }
        pushBackAnimated()
    }
    
    override open func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        resetAnimated()
    }
}


	//MARK: - Helpers

extension UILabel {
    
    func lineHeight(_ height: CGFloat) {
        
        let attributedString = NSMutableAttributedString(string: self.text!)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = height
        attributedString.addAttribute(NSAttributedStringKey.paragraphStyle, value:paragraphStyle, range:NSMakeRange(0, attributedString.length))
        self.attributedText = attributedString
    }
    
}
